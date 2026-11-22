package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Kardum's death trigger and remembers the returned cards for its delayed discard. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null) {
            return;
        }

        UUID sourceId = sourcePermanentId;
        UUID controllerId = entry.getControllerId();
        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(exiled -> sourceId.equals(exiled.sourcePermanentId()))
                .filter(exiled -> controllerId.equals(exiled.ownerId()))
                .toList();
        List<UUID> returnedCardIds = toReturn.stream().map(exiled -> exiled.card().getId()).toList();

        for (ExiledCardEntry exiled : toReturn) {
            Card card = exiled.card();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }
            gameData.addCardToHand(controllerId, card);
            // A card exiled face down goes to a hidden zone without being seen (CR 406.3).
            gameLogService.append(gameData, exiled.faceDown()
                    ? GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " puts a face-down card from exile into their hand.")
                    : GameLog.textCardText(gameData.playerIdToName.get(controllerId) + " puts ", card,
                    " from exile into their hand."));
        }

        if (!returnedCardIds.isEmpty()) {
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    controllerId, entry.getCard(),
                    new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                            returnedCardIds.stream().<CardEffect>map(DiscardSpecificCardEffect::new)
                                    .toArray(CardEffect[]::new)),
                    entry.getSourcePermanentId(), null, null,
                    DelayedReferents.NONE.afterTurn(gameData.turnNumber)));
        }
        log.info("Game {} - {} returns {} card(s) from exile and schedules their next-turn discard",
                gameData.id, entry.getCard().getName(), returnedCardIds.size());
    }
}
