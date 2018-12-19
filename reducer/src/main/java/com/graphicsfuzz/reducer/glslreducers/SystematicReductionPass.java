package com.graphicsfuzz.reducer.glslreducers;

import com.graphicsfuzz.common.transformreduce.ShaderJob;
import com.graphicsfuzz.reducer.reductionopportunities.IReductionOpportunity;
import com.graphicsfuzz.reducer.reductionopportunities.IReductionOpportunityFinder;
import com.graphicsfuzz.reducer.reductionopportunities.ReducerContext;
import java.util.List;
import java.util.Optional;

public class SystematicReductionPass extends AbstractReductionPass {

  private boolean isInitialized;
  private int index;
  private int granularity;

  public SystematicReductionPass(ReducerContext reducerContext,
                          boolean verbose, IReductionOpportunityFinder finder) {
    // Ignore verbose argument for now.
    super(reducerContext, finder);
    this.isInitialized = false;
  }

  @Override
  public Optional<ShaderJob> tryApplyReduction(ShaderJob shaderJob) {
    final ShaderJob workingShaderJob = shaderJob.clone();
    List<? extends IReductionOpportunity> opportunities =
        getFinder().findOpportunities(workingShaderJob, getReducerContext());

    opportunities.sort((first, second) -> second.depth().compareTo(first.depth()));

    if (!isInitialized) {
      isInitialized = true;
      index = 0;
      granularity = Math.max(1, opportunities.size());
    }

    assert granularity > 0;

    if (index >= opportunities.size()) {
      index = 0;
      granularity = Math.max(1, granularity / 2);
      return Optional.empty();
    }


    for (int i = index; i < Math.min(index + granularity, opportunities.size()); i++) {
      opportunities.get(i).applyReduction();
    }

    index += granularity;

    return Optional.of(workingShaderJob);
  }

  @Override
  public void notifyInteresting(boolean interesting) {
    // Ignore.
  }

  @Override
  public void replenish() {
    throw new UnsupportedOperationException(
        "Replenishing is not supported by this kind of reduction pass.");
  }

  @Override
  public boolean reachedMinimumGranularity() {
    if (!isInitialized) {
      // Conceptually we can think that if the pass has not yet been initialized, it is operating
      // at unbounded granularity.
      return false;
    }
    assert granularity != 0;
    return granularity == 1;
  }

}
