package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SagaChapterCounterDistributionSupport {

    public Optional<Distribution> findFixedDistribution(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof DistributeCountersAmongTargetsEffect distribution
                    && distribution.mode() == DivisionMode.CHOSEN
                    && !distribution.etbAssignments()
                    && distribution.total() instanceof Fixed fixed) {
                return Optional.of(new Distribution(distribution.counterType(), fixed.value()));
            }
        }
        return Optional.empty();
    }

    public record Distribution(CounterType counterType, int total) {
    }
}
