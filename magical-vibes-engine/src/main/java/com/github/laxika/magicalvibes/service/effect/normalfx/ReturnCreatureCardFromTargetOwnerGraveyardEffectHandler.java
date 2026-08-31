package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardFromTargetOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class ReturnCreatureCardFromTargetOwnerGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCreatureCardFromTargetOwnerGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID graveyardOwnerId = ((ReturnCreatureCardFromTargetOwnerGraveyardEffect) effect).graveyardOwnerId();
        if (graveyardOwnerId == null) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
        if (graveyard == null) {
            return;
        }

        List<Card> creatureCards = graveyard.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatureCards.isEmpty()) {
            return;
        }
        if (creatureCards.size() == 1) {
            returnCard(gameData, graveyardOwnerId, creatureCards.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(entry.getControllerId(), IntStream.range(0, creatureCards.size()).boxed().toList(),
                        GraveyardChoiceDestination.BATTLEFIELD,
                        entry.getCard().getName() + " — choose a creature card from "
                                + gameData.playerIdToName.get(graveyardOwnerId)
                                + "'s graveyard to return to the battlefield.")
                .cardPool(new ArrayList<>(creatureCards))
                .destinationControllerId(graveyardOwnerId)
                .mandatory(true)
                .build());
    }

    private void returnCard(GameData gameData, UUID graveyardOwnerId, Card card) {
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        graveyardReturnSupport.putCardOntoBattlefield(gameData, graveyardOwnerId, card);
    }
}
