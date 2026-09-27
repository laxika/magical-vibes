package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesOneOfActivatedExiledCreatureCardsEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Starts the opponent choice for cards exiled as an activated ability cost. */
@Component
@RequiredArgsConstructor
public class OpponentChoosesOneOfActivatedExiledCreatureCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesOneOfActivatedExiledCreatureCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> validCardIds = entry.getActivatedAbilityExiledCardIds().stream()
                .filter(cardId -> {
                    ExiledCardEntry exiled = gameData.findExiledCard(cardId);
                    return exiled != null && !exiled.faceDown();
                })
                .toList();
        if (validCardIds.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponentIds.isEmpty()) {
            return;
        }

        String sourceName = entry.getCard() == null ? "The ability" : entry.getCard().getName();
        if (opponentIds.size() == 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ActivatedExiledCardChoice(
                    opponentIds.getFirst(), controllerId, validCardIds, sourceName));
        } else {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ActivatedExiledCardOpponentChoice(
                    controllerId, opponentIds, validCardIds, sourceName));
        }
    }
}
