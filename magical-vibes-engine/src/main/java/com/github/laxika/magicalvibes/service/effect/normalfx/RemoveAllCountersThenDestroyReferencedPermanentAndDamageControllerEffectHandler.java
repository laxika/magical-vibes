package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the combined Bomb Squad-style fuse detonation effect. */
@Component
@RequiredArgsConstructor
public class RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect) effect;
        Permanent referenced = findReferencedPermanent(gameData, entry, e.reference());
        if (referenced == null && (e.reference() != PermanentReference.TRIGGERING
                || entry.getDamageSourceCard() == null)) {
            return;
        }

        UUID controllerId = referenced != null
                ? gameQueryService.findPermanentController(gameData, referenced.getId())
                : entry.getTriggeringPermanentControllerId();
        Card damageSourceCard = referenced != null ? referenced.getCard() : entry.getDamageSourceCard();
        UUID damageSourceId = referenced != null ? referenced.getId() : entry.getTriggeringPermanentId();
        boolean damagePrevented = referenced != null && gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isPreventedFromDealingDamage(gameData, referenced);

        if (referenced != null) {
            int removed = referenced.getCounterCount(e.counterType());
            referenced.setCounterCount(e.counterType(), 0);
            if (e.counterType() == CounterType.OIL) {
                gameData.recordOilCounterRemoved(referenced, removed);
            }
            if (removed > 0) {
                String counterName = permanentCounterSupport.counterTypeName(e.counterType());
                gameLogService.append(gameData, GameLog.builder().card(damageSourceCard)
                        .text(" removes all its " + counterName + " counters (" + removed + ").")
                        .build());
            }

            destructionSupport.tryDestroyAndLog(gameData, referenced, entry.getCard().getName());
        }

        if (controllerId == null || damagePrevented) {
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
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), damageEntry);
        damageSupport.dealDamageToPlayer(gameData, damageEntry, controllerId, rawDamage);
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
