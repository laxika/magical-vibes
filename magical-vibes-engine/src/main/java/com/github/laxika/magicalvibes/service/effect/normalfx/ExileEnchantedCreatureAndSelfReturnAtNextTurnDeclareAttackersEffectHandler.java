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
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached() || aura.getAttachedTo() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        UUID enchantedControllerId = enchanted == null
                ? null : gameQueryService.findPermanentController(gameData, enchanted.getId());
        if (enchanted == null || enchantedControllerId == null) {
            return;
        }

        List<Card> enchantedCards = enchanted.cardsLeavingBattlefield();
        List<Card> auraCards = aura.cardsLeavingBattlefield();
        if (enchantedCards.isEmpty() || auraCards.isEmpty()) {
            return;
        }

        Card enchantedCard = enchantedCards.getFirst();
        Card auraCard = auraCards.getFirst();
        if (!permanentRemovalService.removePermanentToExile(gameData, aura)
                || !permanentRemovalService.removePermanentToExile(gameData, enchanted)) {
            return;
        }

        gameData.queueDelayedAction(new PendingExileReturn(
                enchantedCard,
                enchantedControllerId,
                true,
                false,
                TurnStep.DECLARE_ATTACKERS,
                0,
                List.of(auraCard),
                true,
                false,
                true,
                false,
                null,
                null,
                false,
                false,
                0,
                Set.of(auraCard.getId()),
                null,
                0,
                Map.of(),
                false));

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(enchantedCard,
                " and " + auraCard.getName()
                        + " are exiled. They return at the beginning of their controller's next declare-attackers step."));
        log.info("Game {} - {} and its Aura {} are exiled until the enchanted creature controller's next declare-attackers step",
                gameData.id, enchantedCard.getName(), auraCard.getName());
    }
}
