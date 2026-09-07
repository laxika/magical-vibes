package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneCreatureCardPutIntoGraveyardThisWayEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Begins the resolution-time choice for a spell such as Starfall Invocation. The candidate list
 * is limited to cards that both entered the controller's graveyard during the preceding destroy
 * effect and are still there when the choice begins.
 */
@Component
@RequiredArgsConstructor
public class ReturnOneCreatureCardPutIntoGraveyardThisWayEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOneCreatureCardPutIntoGraveyardThisWayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null || graveyard.isEmpty() || entry.getEventCardIds().isEmpty()) {
            return;
        }

        Set<UUID> destroyedCardIds = new HashSet<>(entry.getEventCardIds());
        List<Integer> validIndices = IntStream.range(0, graveyard.size())
                .filter(index -> {
                    Card card = graveyard.get(index);
                    return destroyedCardIds.contains(card.getId()) && card.hasType(CardType.CREATURE);
                })
                .boxed()
                .toList();
        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(entry.getControllerId(), validIndices, GraveyardChoiceDestination.BATTLEFIELD,
                        "Choose a creature card to return to the battlefield.")
                .mandatory(true)
                .build());
    }
}
