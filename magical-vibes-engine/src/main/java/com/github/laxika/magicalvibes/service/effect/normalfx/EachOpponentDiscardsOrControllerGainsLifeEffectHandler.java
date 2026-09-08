package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves {@link EachOpponentDiscardsOrControllerGainsLifeEffect}. */
@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsOrControllerGainsLifeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsOrControllerGainsLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentDiscardsOrControllerGainsLifeEffect discardEffect =
                (EachOpponentDiscardsOrControllerGainsLifeEffect) effect;
        List<UUID> opponents = orderedOpponents(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }

        Card sourceCard = entry.getCard();
        GainLifeEffect gainLife = new GainLifeEffect(
                new Scaled(new EventValue(), discardEffect.lifeGain()));
        DiscardFollowUp followUp = DiscardFollowUp.eachPlayerWithThenEffect(
                opponents, entry.getControllerId(), 1, sourceCard, gainLife);
        followUp = playerInteractionSupport.startNextEachPlayerDiscard(gameData, followUp);

        if (!gameData.interaction.isAwaitingInput() && followUp.eachPlayerNoDiscardCount() > 0) {
            StackEntry completion = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    entry.getControllerId(),
                    sourceCard.getName() + "'s effect",
                    List.of(gainLife));
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
