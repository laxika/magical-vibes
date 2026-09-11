package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves the mill-and-reanimate effect used by Bind to Life. */
@Component
@RequiredArgsConstructor
public class MillControllerAndPutMilledCreatureOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndPutMilledCreatureOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MillControllerAndPutMilledCreatureOntoBattlefieldEffect e =
                (MillControllerAndPutMilledCreatureOntoBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<Card> creatureCards = graveyardService.resolveMillPlayer(gameData, controllerId, e.count()).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> gameData.playerGraveyards.get(controllerId).stream()
                        .anyMatch(graveyardCard -> graveyardCard.getId().equals(card.getId())))
                .toList();

        if (creatureCards.isEmpty()) {
            return;
        }
        if (creatureCards.size() == 1) {
            Card card = creatureCards.getFirst();
            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, card);
            return;
        }

        List<Integer> indices = IntStream.range(0, creatureCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                        controllerId,
                        indices,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        entry.getCard().getName() + " — choose a creature card to put onto the battlefield.")
                .cardPool(new ArrayList<>(creatureCards))
                .mandatory(true)
                .build());
    }
}
