package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link EachOpponentDiscardsOrControllerDrawsEffect}. */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsOrControllerDrawsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsOrControllerDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> opponents = orderedOpponents(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }

        Card sourceCard = entry.getCard();
        DrawCardEffect drawCards = new DrawCardEffect(new EventValue());
        DiscardFollowUp followUp = DiscardFollowUp.eachPlayerWithThenEffect(
                opponents, entry.getControllerId(), 1, sourceCard, drawCards);
        followUp = playerInteractionSupport.startNextEachPlayerDiscard(gameData, followUp);

        if (!gameData.interaction.isAwaitingInput() && followUp.eachPlayerNoDiscardCount() > 0) {
            StackEntry completion = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    entry.getControllerId(),
                    sourceCard.getName() + "'s effect",
                    List.of(drawCards));
            completion.setEventValue(followUp.eachPlayerNoDiscardCount());
            gameData.stack.add(completion);
        }
    }

    private List<UUID> orderedOpponents(GameData gameData, UUID controllerId) {
        List<UUID> opponents = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)) {
            opponents.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                opponents.add(playerId);
            }
        }
        return opponents;
    }
}
