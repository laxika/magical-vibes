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
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
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
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastCardFromGraveyardEffect castEffect = (CastCardFromGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (castEffect.cards() != null) {
            offerNamedCards(gameData, entry, castEffect);
            return;
        }

        // A card chosen as the ability went on the stack; a player there is the trigger event's ("that player", the
        // player dealt damage), not a chosen card.
        if (entry.getTargetId() != null && !gameData.playerIds.contains(entry.getTargetId())) {
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

    /**
     * The cards the effect's subject names in a graveyard that may be cast: one is offered with a "may", several as
     * offers of one choice group, of which accepting one withdraws the others.
     */
    private void offerNamedCards(GameData gameData, StackEntry entry, CastCardFromGraveyardEffect castEffect) {
        List<Card> named = subjectResolutionService.locatedCards(gameData, entry, castEffect, castEffect.cards())
                .stream()
                .filter(located -> located.zone() == Zone.GRAVEYARD)
                .map(SubjectResolutionService.LocatedCard::card)
                .filter(card -> isLegal(gameData, entry, castEffect, card))
                .toList();
        if (named.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " — no card in a graveyard can be cast this way."));
            return;
        }
        CastCardFromGraveyardEffect offer = named.size() == 1 || castEffect.choiceGroupId() != null
                ? castEffect : castEffect.inChoiceGroup(UUID.randomUUID());
        for (int i = named.size() - 1; i >= 0; i--) {
            offerCast(gameData, entry, offer, named.get(i));
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
                entry.getCard().getName() + " — Cast " + card.getName() + " from " + graveyardName(gameData, entry, card)
                        + (effect.withoutPayingManaCost() ? " without paying its mana cost?" : "?"),
                entry.getSourcePermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                entry.getXValue()));
    }

    private String graveyardName(GameData gameData, StackEntry entry, Card card) {
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        return ownerId == null || ownerId.equals(entry.getControllerId())
                ? "your graveyard" : gameData.playerIdToName.get(ownerId) + "'s graveyard";
    }
}
