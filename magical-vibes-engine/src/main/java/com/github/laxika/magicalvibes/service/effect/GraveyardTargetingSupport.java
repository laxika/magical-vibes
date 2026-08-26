package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardCreateTokenIfCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardWithConditionalBonusEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetedGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraveyardTargetingSupport {

    public Target findTarget(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            CardEffect targetEffect = unwrapTargetingWrappers(effect);
            // A "you may [exile this, then return target cards]" bundle keeps its steps in a
            // SequenceEffect, so the targeting step lives one level deeper (Iname, Life Aspect).
            if (targetEffect instanceof SequenceEffect sequence) {
                Target nested = findTarget(sequence.steps());
                if (nested != null) {
                    return nested;
                }
                continue;
            }
            if (targetEffect instanceof SacrificePermanentThenEffect sacrificeThen) {
                if (sacrificeThen.thenEffect() != null) {
                    Target nested = findTarget(List.of(sacrificeThen.thenEffect()));
                    if (nested != null) {
                        return nested;
                    }
                }
            }
            if (targetEffect instanceof DiscardCardThenEffect discardThen
                    && discardThen.thenEffect() != null) {
                Target nested = findTarget(List.of(discardThen.thenEffect()));
                if (nested != null) {
                    return nested;
                }
            }
            Target target = targetOf(targetEffect);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private CardEffect unwrapTargetingWrappers(CardEffect effect) {
        CardEffect unwrapped = effect;
        while (true) {
            if (unwrapped instanceof MayEffect may) {
                unwrapped = may.wrapped();
            } else if (unwrapped instanceof ConditionalEffect conditional) {
                unwrapped = conditional.wrapped();
            } else if (unwrapped instanceof MayPayManaEffect mayPay) {
                unwrapped = mayPay.wrapped();
            } else {
                return unwrapped;
            }
        }
    }

    private Target targetOf(CardEffect effect) {
        if (effect instanceof ExileCardsFromGraveyardEffect exile) {
            return new Target(null, GraveyardSearchScope.ALL_GRAVEYARDS, "to exile",
                    exile.maxTargets(), 0);
        }
        if (effect instanceof ExileCardFromGraveyardThenEffect exileThen) {
            return findTarget(List.of(exileThen.thenEffect()));
        }
        if (effect instanceof ExileGraveyardCardWithConditionalBonusEffect exile) {
            return new Target(exile.filter(), exile.graveyardScope(), "to exile", 1, 1);
        }
        if (effect instanceof ExileGraveyardCardCreateTokenIfCreatureEffect exileCreature) {
            return new Target(exileCreature.filter(), exileCreature.graveyardScope(), "to exile", 1, 1);
        }
        if (effect instanceof ExileGraveyardCardsEffect exile) {
            GraveyardSearchScope scope = effect.targetSpec().graveyardScope().orElse(null);
            if (scope != null) {
                boolean anyNumber = exile.scope()
                        == com.github.laxika.magicalvibes.model.effect.GraveyardExileScope.TARGET_CARDS_CONTROLLER_GRAVEYARD;
                int minTargets = anyNumber ? 0
                        : exile.exactTargetCount() ? exile.count() : Math.min(1, exile.count());
                int maxTargets = anyNumber ? Integer.MAX_VALUE : exile.count();
                return new Target(exile.filter(), scope, "to exile", maxTargets, minTargets);
            }
        }
        if (effect instanceof ExileTargetCardFromGraveyardAndImprintOnSourceEffect imprint) {
            return new Target(imprint.filter(), imprint.scope(), "to exile", 1, 1);
        }
        if (effect instanceof ExileTargetCardFromGraveyardAndCreateTokenCopyEffect copy) {
            GraveyardSearchScope scope = copy.targetSpec().graveyardScope().orElseThrow();
            return new Target(copy.filter(), scope, "to exile and copy", 1, 0);
        }
        if (effect instanceof ExileTargetCardFromGraveyardAndMayCastCopyEffect copy) {
            return new Target(copy.filter(), copy.scope(), "to exile", 1, 1);
        }
        if (effect instanceof ExileTargetCardFromGraveyardPutCounterOnSourceEffect) {
            return new Target(null, GraveyardSearchScope.ALL_GRAVEYARDS, "to exile", 1, 1);
        }
        if (effect instanceof GrantTargetGraveyardCardCastEffect grantCast) {
            return new Target(grantCast.filter(), grantCast.scope(), "to cast", 1, 1);
        }
        if (effect instanceof GrantFlashbackToTargetGraveyardCardEffect grantFlashback) {
            CardPredicate filter = new CardAnyOfPredicate(grantFlashback.cardTypes().stream()
                    .map(type -> (CardPredicate) new CardTypePredicate(type))
                    .toList());
            return new Target(filter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                    "to gain flashback", 1, 1);
        }
        if (effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnTargets) {
            return new Target(returnTargets.filter(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                    "to your hand", returnTargets.maxTargets(), returnTargets.minTargets());
        }
        if (effect instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect returnTargets) {
            int maxTargets = returnTargets.xScaled() ? 1
                    : returnTargets.hasTotalManaValueCap() ? Integer.MAX_VALUE : returnTargets.maxTargets();
            int minTargets = returnTargets.xScaled() ? 1 : 0;
            return new Target(returnTargets.filter(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                    "to the battlefield", maxTargets, minTargets);
        }
        if (effect instanceof TargetedGraveyardCardsEffect targetCards) {
            int maxTargets = targetCards.maxTargets() == 0
                    ? Integer.MAX_VALUE : targetCards.maxTargets();
            return new Target(targetCards.filter(), targetCards.source(),
                    "into its owner's library", maxTargets, 0);
        }
        if (effect instanceof ReturnCardFromGraveyardEffect returnEffect && returnEffect.targetGraveyard()) {
            String destination = switch (returnEffect.destination()) {
                case HAND -> "to your hand";
                case BATTLEFIELD -> "to the battlefield";
                case TOP_OF_OWNERS_LIBRARY -> "on top of its owner's library";
                case TOP_OF_CONTROLLERS_LIBRARY -> "on top of your library";
                case BOTTOM_OF_OWNERS_LIBRARY -> "on the bottom of its owner's library";
                case SHUFFLE_INTO_OWNERS_LIBRARY -> "into its owner's library";
                case EXILE -> "to exile";
                case DREDGE -> "with dredge";
                case MAY_ABILITY_TARGET, COPY_ON_ENTER -> "as chosen";
            };
            return new Target(returnEffect.filter(), returnEffect.source(), destination, 1,
                    returnEffect.upTo() ? 0 : 1);
        }
        return null;
    }

    /**
     * A graveyard-targeting step found on a trigger's effects.
     *
     * @param maxTargets how many graveyard cards the step may target — {@code 1} for the
     *                   single-target effects, larger for "up to N"/"any number of target cards"
     * @param minTargets how many graveyard targets the step requires
     */
    public record Target(CardPredicate filter, GraveyardSearchScope scope, String destination,
                         int maxTargets, int minTargets) {

        public Target(CardPredicate filter, GraveyardSearchScope scope, String destination, int maxTargets) {
            this(filter, scope, destination, maxTargets, Math.min(1, maxTargets));
        }
    }
}
