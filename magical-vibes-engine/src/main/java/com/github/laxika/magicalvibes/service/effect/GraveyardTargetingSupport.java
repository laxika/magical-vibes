package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraveyardTargetingSupport {

    public Target findTarget(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            CardEffect targetEffect = unwrapMay(effect);
            // A "you may [exile this, then return target cards]" bundle keeps its steps in a
            // SequenceEffect, so the targeting step lives one level deeper (Iname, Life Aspect).
            if (targetEffect instanceof SequenceEffect sequence) {
                Target nested = findTarget(sequence.steps());
                if (nested != null) {
                    return nested;
                }
                continue;
            }
            Target target = targetOf(targetEffect);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private CardEffect unwrapMay(CardEffect effect) {
        return effect instanceof MayEffect may ? may.wrapped() : effect;
    }

    private Target targetOf(CardEffect effect) {
        if (effect instanceof ExileGraveyardCardWithConditionalBonusEffect) {
            return new Target(null, GraveyardSearchScope.ALL_GRAVEYARDS, "to exile", 1);
        }
        if (effect instanceof ExileGraveyardCardCreateTokenIfCreatureEffect exileCreature) {
            return new Target(exileCreature.filter(), GraveyardSearchScope.ALL_GRAVEYARDS, "to exile", 1);
        }
        if (effect instanceof ExileGraveyardCardsEffect exile) {
            GraveyardSearchScope scope = effect.targetSpec().graveyardScope().orElse(null);
            if (scope != null) {
                return new Target(exile.filter(), scope, "to exile", 1);
            }
        }
        if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect imprint) {
            return new Target(imprint.filter(), imprint.scope(), "to exile", 1);
        }
        if (effect instanceof GrantTargetGraveyardCardCastEffect grantCast) {
            return new Target(grantCast.filter(), grantCast.scope(), "to cast", 1);
        }
        if (effect instanceof GrantFlashbackToTargetGraveyardCardEffect grantFlashback) {
            CardPredicate filter = new CardAnyOfPredicate(grantFlashback.cardTypes().stream()
                    .map(type -> (CardPredicate) new CardTypePredicate(type))
                    .toList());
            return new Target(filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                    "to gain flashback", 1);
        }
        if (effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnTargets) {
            return new Target(returnTargets.filter(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                    "to your hand", returnTargets.maxTargets());
        }
        if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
            String destination = switch (returnEffect.destination()) {
                case HAND -> "to your hand";
                case BATTLEFIELD -> "to the battlefield";
                case TOP_OF_OWNERS_LIBRARY -> "on top of its owner's library";
                case BOTTOM_OF_OWNERS_LIBRARY -> "on the bottom of its owner's library";
                case SHUFFLE_INTO_OWNERS_LIBRARY -> "into its owner's library";
                case EXILE -> "to exile";
                case MAY_ABILITY_TARGET -> "as chosen";
            };
            return new Target(returnEffect.filter(), returnEffect.source(), destination, 1);
        }
        return null;
    }

    /**
     * A graveyard-targeting step found on a trigger's effects.
     *
     * @param maxTargets how many graveyard cards the step may target — {@code 1} for the
     *                   single-target effects, larger for "up to N"/"any number of target cards"
     */
    public record Target(CardPredicate filter, GraveyardSearchScope scope, String destination, int maxTargets) {
    }
}
