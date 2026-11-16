package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Returns a card remembered by a token's leaves-the-battlefield trigger to the bound hand. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnExiledCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnExiledCardToHandEffect.class;
    }

    /**
     * The delayed triggered ability "at the beginning of the next end step, return those cards to {@code ownerId}'s
     * hand, then draw {@code thenDraw} cards" that a spell or ability of {@code sourceCard} controlled by
     * {@code controllerId} made after exiling {@code cardIds}. The ability returns whichever of the cards are still in
     * exile as it resolves; with {@code onlyIfStillExiled} it triggers only if one still is ("if you haven't cast it",
     * CR 603.4), and the cards that left exile and came back are new objects it doesn't return (CR 400.7).
     */
    public static TemporaryGlobalTriggeredAbility returnAtNextEndStep(GameData gameData, UUID controllerId,
                                                                      Card sourceCard, UUID sourcePermanentId,
                                                                      List<UUID> cardIds, UUID ownerId, int thenDraw,
                                                                      boolean onlyIfStillExiled) {
        return returnAtEndStep(ActivePlayer.ANY, gameData, controllerId, sourceCard, sourcePermanentId, cardIds,
                ownerId, thenDraw, onlyIfStillExiled);
    }

    /**
     * The delayed triggered ability "at the beginning of your next end step, put that card into your hand" that a
     * spell or ability of {@code sourceCard} controlled by {@code controllerId} made after exiling {@code cardId}
     * (Necropotence). It returns the card if it is still in exile as it resolves (CR 603.7c).
     */
    public static TemporaryGlobalTriggeredAbility returnAtYourNextEndStep(GameData gameData, UUID controllerId,
                                                                          Card sourceCard, UUID sourcePermanentId,
                                                                          UUID cardId, UUID ownerId) {
        return returnAtEndStep(ActivePlayer.YOU, gameData, controllerId, sourceCard, sourcePermanentId,
                List.of(cardId), ownerId, 0, false);
    }

    private static TemporaryGlobalTriggeredAbility returnAtEndStep(ActivePlayer endStepOf, GameData gameData,
                                                                   UUID controllerId, Card sourceCard,
                                                                   UUID sourcePermanentId, List<UUID> cardIds,
                                                                   UUID ownerId, int thenDraw,
                                                                   boolean onlyIfStillExiled) {
        List<CardEffect> effects = new ArrayList<>();
        cardIds.forEach(cardId -> effects.add(new ReturnExiledCardToHandEffect(cardId, ownerId)));
        if (thenDraw > 0) {
            effects.add(new DrawCardEffect(thenDraw));
        }
        TriggerOptions options = onlyIfStillExiled
                ? TriggerOptions.DEFAULT.withInterveningIf(
                        new CardsStillInZone(Zone.EXILE, gameData.exileEntryVersionsOf(cardIds)))
                : TriggerOptions.DEFAULT;
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                new TriggeredAbility(Triggers.endStep(endStepOf), options, effects.toArray(CardEffect[]::new)),
                sourcePermanentId, null, null, DelayedReferents.cards(cardIds));
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnExiledCardToHandEffect returnEffect = (ReturnExiledCardToHandEffect) effect;
        UUID cardId = returnEffect.exiledCardId();
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !gameData.removeFromExile(cardId)) {
            return;
        }

        Card card = exiled.card();
        UUID handPlayerId = returnEffect.handPlayerId() != null
                ? returnEffect.handPlayerId() : exiled.ownerId();
        gameData.addCardToHand(handPlayerId, card);
        String playerName = gameData.playerIdToName.get(handPlayerId);
        if (exiled.faceDown()) {
            gameLogService.append(gameData, GameLog.text(playerName + " puts a face-down card from exile into their hand."));
        } else {
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", card,
                    " from exile into their hand."));
        }
        log.info("Game {} - {} returns {} from exile to hand", gameData.id, playerName, card.getName());
    }
}
