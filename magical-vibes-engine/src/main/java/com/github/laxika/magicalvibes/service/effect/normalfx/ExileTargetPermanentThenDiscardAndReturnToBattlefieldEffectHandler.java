package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutDiscardedCardOnTopOfLibraryThenBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card targetCard = target.getCard();
        UUID targetCardId = targetCard.getId();
        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }
        gameLogService.append(gameData, GameLog.isExiled(targetCard));
        permanentRemovalService.removeOrphanedAuras(gameData);

        ExiledCardEntry exiled = gameData.findExiledCard(targetCardId);
        if (exiled == null || !gameData.removeFromExile(targetCardId)) {
            return;
        }

        UUID ownerId = exiled.ownerId();
        Card card = exiled.card();
        gameData.addCardToHand(ownerId, card);
        triggerCollectionService.checkPermanentReturnedToHandTriggers(gameData, ownerId);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", card, " to their hand."));

        List<Card> hand = gameData.playerHands.get(ownerId);
        if (hand == null) {
            return;
        }
        int cardIndex = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(targetCardId)) {
                cardIndex = i;
                break;
            }
        }
        if (cardIndex < 0) {
            return;
        }

        gameData.discardCausedByOpponent = !Objects.equals(entry.getControllerId(), ownerId);
        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, ownerId)) {
            return;
        }

        DiscardFollowUp followUp = DiscardFollowUp.thenEffect(
                        entry.getCard(), new PutDiscardedCardOnTopOfLibraryThenBattlefieldEffect())
                .withSourceContext(entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot(), entry.getEventValue());
        playerInputService.beginDiscardChoice(gameData, ownerId, List.of(cardIndex),
                entry.getCard().getName() + " - Choose that card to discard.", 1, followUp);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(ownerId) + " is choosing that card to discard."));
        log.info("Game {} - {} is choosing {} to discard for {}",
                gameData.id, gameData.playerIdToName.get(ownerId), card.getName(), entry.getCard().getName());
    }
}
