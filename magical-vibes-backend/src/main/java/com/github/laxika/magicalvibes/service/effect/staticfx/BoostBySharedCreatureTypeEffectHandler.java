package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoostBySharedCreatureTypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostBySharedCreatureTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent target = context.target();
        GameData gameData = context.gameData();

        List<CardSubtype> targetTypes = new ArrayList<>(target.getCard().getSubtypes());
        targetTypes.addAll(target.getTransientSubtypes());
        boolean targetIsChangeling = target.hasKeyword(Keyword.CHANGELING);

        if (targetTypes.isEmpty() && !targetIsChangeling) return;

        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);
        final int[] count = {0};

        gameData.forEachPermanent((playerId, other) -> {
            if (other == target) return;
            if (!support.isEffectivelyCreature(other, hasAnimateArtifacts)) return;

            List<CardSubtype> otherTypes = new ArrayList<>(other.getCard().getSubtypes());
            otherTypes.addAll(other.getTransientSubtypes());
            boolean otherIsChangeling = other.hasKeyword(Keyword.CHANGELING);

            if (otherTypes.isEmpty() && !otherIsChangeling) return;

            boolean sharesType = (targetIsChangeling && (otherIsChangeling || !otherTypes.isEmpty()))
                    || (otherIsChangeling && !targetTypes.isEmpty())
                    || targetTypes.stream().anyMatch(otherTypes::contains);

            if (sharesType) count[0]++;
        });

        accumulator.addPower(count[0]);
        accumulator.addToughness(count[0]);
    }
}
