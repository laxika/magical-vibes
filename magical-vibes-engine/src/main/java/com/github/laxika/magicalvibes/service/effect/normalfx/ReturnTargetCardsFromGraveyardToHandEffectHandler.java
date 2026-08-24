package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReturnTargetCardsFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final MayPayManaEffectHandler mayPayManaEffectHandler;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    public ReturnTargetCardsFromGraveyardToHandEffectHandler(GraveyardReturnSupport graveyardReturnSupport) {
        this(graveyardReturnSupport, null, null, null);
    }

    public ReturnTargetCardsFromGraveyardToHandEffectHandler(
            GraveyardReturnSupport graveyardReturnSupport,
            MayPayManaEffectHandler mayPayManaEffectHandler) {
        this(graveyardReturnSupport, mayPayManaEffectHandler, null, null);
    }

    @Autowired
    public ReturnTargetCardsFromGraveyardToHandEffectHandler(
            GraveyardReturnSupport graveyardReturnSupport,
            MayPayManaEffectHandler mayPayManaEffectHandler,
            InteractionHandlerRegistry interactionHandlerRegistry,
            PredicateEvaluationService predicateEvaluationService) {
        this.graveyardReturnSupport = graveyardReturnSupport;
        this.mayPayManaEffectHandler = mayPayManaEffectHandler;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardsFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCardsFromGraveyardToHandEffect) effect;
        if (e.unlessAnyPlayerPaysX()) {
            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            if (effectIndex >= 0 && mayPayManaEffectHandler != null) {
                String manaCost = "{" + entry.getXValue() + "}";
                entry.replaceEffectToResolve(effectIndex, new MayPayManaEffect(
                        manaCost,
                        null,
                        "Pay " + manaCost + " to prevent " + entry.getCard().getName() + "'s effect?",
                        MayPayPayer.ANY_PLAYER,
                        e.withoutAnyPlayerPaysX(),
                        0,
                        false));
                mayPayManaEffectHandler.resolve(gameData, entry, entry.getEffectsToResolve().get(effectIndex));
                return;
            }
        }

        if (e.opponentChoosesOneForHand()) {
            resolveOpponentChoice(gameData, entry, e);
            return;
        }

        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                entry.getTargetCardIdsForEffect(effect),
                (graveyard, card) -> gameData.addCardToHand(entry.getControllerId(), card),
                " returns ", " from graveyard to hand.");
    }

    private void resolveOpponentChoice(GameData gameData, StackEntry entry,
                                       ReturnTargetCardsFromGraveyardToHandEffect effect) {
        List<Card> legalTargets = legalTargets(gameData, entry, effect);
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        UUID chosenCardId = state.wakeToSlaughterChosenCardId;
        state.wakeToSlaughterChosenCardId = null;

        if (chosenCardId != null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            Card chosen = legalTargets.stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .orElse(null);
            if (chosen != null) {
                applyOpponentChoice(gameData, entry, effect, legalTargets, chosen);
            }
            return;
        }

        if (legalTargets.isEmpty()) {
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .findFirst()
                .orElse(null);
        if (legalTargets.size() == 1 || opponentId == null) {
            applyOpponentChoice(gameData, entry, effect, legalTargets, legalTargets.getFirst());
            return;
        }

        state.resolutionTimeWakeToSlaughterResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        List<Integer> indices = IntStream.range(0, legalTargets.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(opponentId, indices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        entry.getCard().getName()
                                + " — choose a creature card to return to its owner's hand."
                                + " The other returns to the battlefield under your control with haste.")
                .cardPool(new ArrayList<>(legalTargets))
                .mandatory(true)
                .build());
    }

    private List<Card> legalTargets(GameData gameData, StackEntry entry,
                                    ReturnTargetCardsFromGraveyardToHandEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null || graveyard.isEmpty()) {
            return List.of();
        }
        return entry.getTargetCardIdsForEffect(effect).stream()
                .map(cardId -> graveyard.stream()
                        .filter(card -> card.getId().equals(cardId))
                        .findFirst()
                        .orElse(null))
                .filter(card -> card != null
                        && (effect.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), entry.getCard().getId(), gameData, entry.getControllerId())))
                .toList();
    }

    private void applyOpponentChoice(GameData gameData, StackEntry entry,
                                      ReturnTargetCardsFromGraveyardToHandEffect effect,
                                      List<Card> legalTargets, Card handCard) {
        graveyardReturnSupport.processTargetedGraveyardTargets(gameData, entry,
                List.of(handCard.getId()),
                (graveyard, card) -> gameData.addCardToHand(entry.getControllerId(), card),
                " returns ", " from graveyard to hand.");

        legalTargets.stream()
                .filter(card -> !card.getId().equals(handCard.getId()))
                .findFirst()
                .ifPresent(card -> graveyardReturnSupport.resolvePreTargetedById(
                        gameData,
                        entry,
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(effect.filter())
                                .grantHaste(true)
                                .exileAtEndStep(true)
                                .build(),
                        entry.getControllerId(),
                        entry.getCard().getId(),
                        card.getId()));
    }
}
