package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the combined Bomb Squad-style fuse detonation effect: removes the counters, destroys the creature as the
 * resolving step ({@link StepDepartureSupport#destroyThenContinue}), so its CR 616.1 questions are asked before it
 * leaves, and then has it deal the damage to its controller. The creature deals the damage whether or not it was
 * destroyed, and even when it already left the battlefield, as it last existed there (the card's rulings; CR 608.2h),
 * so its card, controller and whether its damage is prevented are taken before the destruction.
 */
@Component
@RequiredArgsConstructor
public class RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffectHandler
        implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final DamageSupport damageSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect) effect;
        Permanent referenced = findReferencedPermanent(gameData, entry, e.reference());
        if (referenced == null) {
            if (e.reference() == PermanentReference.TRIGGERING && entry.getDamageSourceCard() != null) {
                dealDamage(gameData, entry.getTriggeringPermanentControllerId(), entry.getDamageSourceCard(),
                        entry.getTriggeringPermanentId(), e.damage());
            }
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, referenced.getId());
        boolean damagePrevented = gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, referenced);

        int removed = referenced.getCounterCount(e.counterType());
        referenced.setCounterCount(e.counterType(), 0);
        if (e.counterType() == CounterType.OIL) {
            gameData.recordOilCounterRemoved(referenced, removed);
        }
        if (removed > 0) {
            String counterName = permanentCounterSupport.counterTypeName(e.counterType());
            gameLogService.append(gameData, GameLog.builder().card(referenced.getCard())
                    .text(" removes all its " + counterName + " counters (" + removed + ").")
                    .build());
        }

        if (controllerId == null || damagePrevented) {
            stepDepartureSupport.destroy(gameData, entry, effect, List.of(referenced), false);
            return;
        }
        entry.rememberLastKnownPermanentCard(referenced.getId(), referenced.getCard());
        entry.rememberRemovedPermanentController(referenced.getId(), controllerId);
        stepDepartureSupport.destroyThenContinue(gameData, entry, effect, referenced.getId(), false, this);
    }

    /** The destroyed (or saved) creature deals the damage to the controller it had before the destruction. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect) effect;
        for (DepartureResult result : entry.getResolutionResults().departures()) {
            Card card = entry.lastKnownPermanentCard(result.permanentId());
            UUID controllerId = entry.lastKnownPermanentController(result.permanentId());
            if (card != null && controllerId != null) {
                dealDamage(gameData, controllerId, card, result.permanentId(), e.damage());
            }
        }
    }

    private void dealDamage(GameData gameData, UUID controllerId, Card damageSourceCard, UUID damageSourceId,
                            int damage) {
        if (controllerId == null) {
            return;
        }
        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                damageSourceCard,
                controllerId,
                damageSourceCard.getName() + "'s ability",
                List.of(),
                null,
                damageSourceId);
        damageSupport.dealDamageToPlayer(gameData, damageEntry, controllerId, damage);
    }

    private Permanent findReferencedPermanent(GameData gameData, StackEntry entry,
                                              PermanentReference reference) {
        return switch (reference) {
            case SOURCE -> findPermanent(gameData, entry.getSourcePermanentId());
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
            case ATTACHED -> {
                Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
                yield source == null || !source.isAttached()
                        ? null
                        : findPermanent(gameData, source.getAttachedTo());
            }
            case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
        };
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }
}
