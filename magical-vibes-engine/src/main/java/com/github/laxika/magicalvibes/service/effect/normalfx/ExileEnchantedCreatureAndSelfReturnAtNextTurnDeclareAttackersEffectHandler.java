package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureAndSelfReturnAtNextTurnDeclareAttackersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves Meandered Towershell's delayed exile and attached return. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileEnchantedCreatureAndSelfReturnAtNextTurnDeclareAttackersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEnchantedCreatureAndSelfReturnAtNextTurnDeclareAttackersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The creature is the one that attacked when the trigger went on the stack, so moving or
        // destroying the Aura afterwards does not change which creature is exiled.
        Permanent enchanted = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        UUID enchantedControllerId = enchanted == null
                ? null : gameQueryService.findPermanentController(gameData, enchanted.getId());
        if (enchanted == null || enchantedControllerId == null) {
            return;
        }

        Permanent aura = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        List<Card> enchantedCards = enchanted.cardsLeavingBattlefield();
        List<Card> auraCards = aura == null ? List.of() : aura.cardsLeavingBattlefield();
        if (enchantedCards.isEmpty() || (aura != null && auraCards.isEmpty())) {
            return;
        }

        Card enchantedCard = enchantedCards.getFirst();
        if (aura != null && !permanentRemovalService.removePermanentToExile(gameData, aura)) {
            return;
        }
        if (!permanentRemovalService.removePermanentToExile(gameData, enchanted)) {
            return;
        }

        List<Card> returningAuras = aura == null ? List.of() : List.of(auraCards.getFirst());
        Set<UUID> attachToCardIds = aura == null ? Set.of() : Set.of(auraCards.getFirst().getId());
        PendingExileReturn pendingReturn = new PendingExileReturn(
                enchantedCard,
                enchantedControllerId,
                true,
                false,
                TurnStep.DECLARE_ATTACKERS,
                0,
                returningAuras,
                true,
                false,
                true,
                false,
                null,
                null,
                false,
                false,
                0,
                attachToCardIds,
                null,
                0,
                Map.of(),
                false);
        gameData.queueDelayedAction(pendingReturn);
        gameData.pendingExileReturnEarliestTurns.put(pendingReturn, gameData.turnNumber + 1);

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(enchantedCard, aura == null
                ? " is exiled. It returns at the beginning of its controller's next declare-attackers step."
                : " and " + auraCards.getFirst().getName()
                + " are exiled. They return at the beginning of their controller's next declare-attackers step."));
        log.info("Game {} - {} is exiled until the controller's next declare-attackers step",
                gameData.id, enchantedCard.getName());
    }
}
