package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.ReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndReturnMilledCardToHandOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Resolves a mill followed by a mandatory return-or-token choice. */
@Component
@RequiredArgsConstructor
public class MillControllerAndReturnMilledCardToHandOrCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndReturnMilledCardToHandOrCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var millEffect = (MillControllerAndReturnMilledCardToHandOrCreateTokenEffect) effect;
        UUID controllerId = entry.getControllerId();
        // CR 701.17c: a replacement effect that exiles the milled card instead (Leyline of the Void) leaves it
        // findable in the public zone it moved to, so it is still a milled card that can be put into hand.
        List<Card> milled = graveyardService.resolveMillPlayerIncludingExiled(
                gameData, controllerId, millEffect.count());

        Set<UUID> matchingMilledIds = milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, millEffect.filter(), entry.getCard().getId(), gameData, controllerId))
                .map(Card::getId)
                .collect(Collectors.toSet());
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Integer> validIndices = graveyard == null ? List.of() : IntStream.range(0, graveyard.size())
                .filter(index -> matchingMilledIds.contains(graveyard.get(index).getId()))
                .boxed()
                .toList();

        if (validIndices.isEmpty()) {
            List<ExiledCardEntry> exiledMatches = gameData.exiledCards.stream()
                    .filter(exiled -> !exiled.faceDown() && matchingMilledIds.contains(exiled.card().getId()))
                    .toList();
            if (exiledMatches.size() == 1) {
                ExiledCardEntry exiled = exiledMatches.getFirst();
                gameData.exiledCards.remove(exiled);
                permanentRemovalService.addCardToHandFromGraveyard(gameData, null, exiled.ownerId(), exiled.card());
                return;
            }
            if (exiledMatches.size() > 1) {
                UUID groupId = UUID.randomUUID();
                for (int i = exiledMatches.size() - 1; i >= 0; i--) {
                    Card card = exiledMatches.get(i).card();
                    gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                            card, controllerId,
                            List.of(new ReturnMilledPermanentToHandEffect(groupId, millEffect.filter())),
                            "Put " + card.getName() + " into your hand?"));
                }
                return;
            }
            createTokenEffectHandler.resolve(gameData, entry, millEffect.fallbackToken());
            return;
        }

        String filterLabel = CardPredicateUtils.describeFilter(millEffect.filter());
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, validIndices, GraveyardChoiceDestination.HAND,
                        "Choose " + filterLabel + " milled this way to put into your hand.")
                .mandatory(true)
                .build());
    }
}
