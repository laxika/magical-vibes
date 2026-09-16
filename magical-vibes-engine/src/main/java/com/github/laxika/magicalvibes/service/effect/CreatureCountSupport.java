package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CountAsCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

/** Shared rules for permanents that contribute more than one creature to creature counts. */
public final class CreatureCountSupport {

    private CreatureCountSupport() {
    }

    /** Returns whether a permanent filter includes the creature-count characteristic. */
    public static boolean countsCreatures(PermanentPredicate predicate) {
        if (predicate instanceof PermanentIsCreaturePredicate) {
            return true;
        }
        if (predicate instanceof PermanentAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(CreatureCountSupport::countsCreatures);
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(CreatureCountSupport::countsCreatures);
        }
        return false;
    }

    /** Returns the number contributed by one permanent after its static effects are applied. */
    public static int creatureCount(GameData gameData, Permanent permanent, GameQueryService gameQueryService) {
        if (GameQueryService.isStaticEvaluationActive()) {
            int count = 1;
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CountAsCreaturesEffect countEffect) {
                    count = Math.max(count, countEffect.count());
                }
            }
            return count;
        }
        return gameQueryService.computeStaticBonus(gameData, permanent).creatureCount();
    }
}
