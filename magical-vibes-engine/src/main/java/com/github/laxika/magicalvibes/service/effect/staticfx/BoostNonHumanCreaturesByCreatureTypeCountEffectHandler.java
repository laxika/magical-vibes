package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostNonHumanCreaturesByCreatureTypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class BoostNonHumanCreaturesByCreatureTypeCountEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    public BoostNonHumanCreaturesByCreatureTypeCountEffectHandler(
            StaticEffectSupport support, GameQueryService gameQueryService) {
        this.support = support;
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostNonHumanCreaturesByCreatureTypeCountEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostNonHumanCreaturesByCreatureTypeCountEffect) effect;
        if (!context.targetOnSameBattlefield()) return;

        Permanent target = context.target();
        if (!support.matchesStaticLeaf(target, new PermanentIsCreaturePredicate())
                || support.matchesStaticLeaf(target, new PermanentHasSubtypePredicate(CardSubtype.HUMAN))) {
            return;
        }

        int creatureTypeCount = effectiveCreatureTypeCount(target);
        int amount = Math.min(creatureTypeCount, boost.maximum());
        accumulator.addPower(amount);
        accumulator.addToughness(amount);
    }

    private int effectiveCreatureTypeCount(Permanent target) {
        CharacteristicState layered = LayerSystemService.activeStateFor(target.getId());
        if (layered != null) {
            if (layered.hasKeyword(Keyword.CHANGELING)) {
                return allCreatureTypeCount();
            }
            return (int) layered.getSubtypes().stream()
                    .filter(gameQueryService::isCreatureSubtype)
                    .count();
        }
        if (target.hasKeyword(Keyword.CHANGELING)) {
            return allCreatureTypeCount();
        }
        Set<CardSubtype> subtypes = new HashSet<>(target.getCard().getSubtypes());
        subtypes.addAll(target.getTransientSubtypes());
        subtypes.addAll(target.getGrantedSubtypes());
        return (int) subtypes.stream().filter(gameQueryService::isCreatureSubtype).count();
    }

    private int allCreatureTypeCount() {
        int count = 0;
        for (CardSubtype subtype : CardSubtype.values()) {
            if (gameQueryService.isCreatureSubtype(subtype)) count++;
        }
        return count;
    }
}
