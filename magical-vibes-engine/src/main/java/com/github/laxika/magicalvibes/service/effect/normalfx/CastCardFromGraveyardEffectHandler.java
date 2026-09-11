package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class CastCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastCardFromGraveyardEffect castEffect = (CastCardFromGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (entry.getTargetId() != null) {
            Card selectedCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
            if (selectedCard == null || !isLegal(gameData, entry, castEffect, selectedCard)) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (the chosen card is no longer legal)."));
                return;
            }
            offerCast(gameData, entry, castEffect, selectedCard);
            return;
        }

        List<Card> matchingCards = new ArrayList<>();
        for (UUID graveyardOwnerId : castEffect.scope().graveyardOwners(gameData.orderedPlayerIds, controllerId)) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            if (graveyard != null) {
                matchingCards.addAll(graveyard.stream()
                        .filter(card -> isLegal(gameData, entry, castEffect, card))
                        .toList());
            }
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " — no matching card in the graveyard."));
        } else if (matchingCards.size() == 1) {
            offerCast(gameData, entry, castEffect, matchingCards.getFirst());
        } else {
            entry.setNonTargeting(true);
            gameData.resolvedMayTargetingEntry = entry;
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice.builder(
                            controllerId,
                            IntStream.range(0, matchingCards.size()).boxed().toList(),
                            GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                            "Choose a card from your graveyard to cast.")
                    .cardPool(matchingCards)
                    .mandatory(true)
                    .build());
        }
    }

    private boolean isLegal(GameData gameData, StackEntry entry,
                            CastCardFromGraveyardEffect effect, Card card) {
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        Card adventureFace = card.getBackFaceCard();
        return graveyardOwnerId != null
                && effect.scope().graveyardOwners(gameData.orderedPlayerIds, entry.getControllerId())
                        .contains(graveyardOwnerId)
                && (predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), entry.getCard().getId(), gameData, graveyardOwnerId,
                        entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(), entry.getXValue())
                || (effect.allowAdventure() && isCastableAdventure(card)
                && predicateEvaluationService.matchesCardPredicate(
                        adventureFace, effect.filter(), entry.getCard().getId(), gameData, graveyardOwnerId,
                        entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(), entry.getXValue())));
    }

    private boolean isCastableAdventure(Card card) {
        Card adventureFace = card.getBackFaceCard();
        return !card.hasType(CardType.LAND)
                && card.getCastingOption(AdventureCast.class).isPresent()
                && adventureFace != null
                && (adventureFace.hasType(CardType.INSTANT) || adventureFace.hasType(CardType.SORCERY));
    }

    private void offerCast(GameData gameData, StackEntry entry,
                           CastCardFromGraveyardEffect effect, Card card) {
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                card,
                entry.getControllerId(),
                List.of(effect),
                entry.getCard().getName() + " — Cast " + card.getName() + " from your graveyard?",
                entry.getSourcePermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue()));
    }
}
