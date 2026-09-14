package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.effect.ActivationCostCardReferenceEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.HandCardCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlanarAbilityService {
    private final GameQueryService query;
    private final TargetLegalityService targeting;
    private final EffectResolutionService resolution;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Autowired
    @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    @Lazy
    private StateBasedActionService stateBasedActionService;
    @Autowired
    @Lazy
    private GameMutationCoordinator mutationCoordinator;

    public PlanarAbilityService(@Lazy GameQueryService query, @Lazy TargetLegalityService targeting,
                                @Lazy EffectResolutionService resolution,
                                @Lazy PredicateEvaluationService predicateEvaluationService,
                                @Lazy ExileService exileService, @Lazy GameLogService gameLogService) {
        this.query = query;
        this.targeting = targeting;
        this.resolution = resolution;
        this.predicateEvaluationService = predicateEvaluationService;
        this.exileService = exileService;
        this.gameLogService = gameLogService;
    }

    public PlanarObject source(GameData game, UUID id) {
        if (game.planechase == null) throw new IllegalStateException("This is not a Planechase game");
        return game.planechase.faceUp.stream().filter(source -> source.getId().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalStateException("That planar object is no longer face up"));
    }

    public ActivatedAbility ability(PlanarObject source, int index) {
        List<ActivatedAbility> abilities = source.getCard().getActivatedAbilities();
        if (index < 0 || index >= abilities.size()) throw new IllegalArgumentException("Invalid ability index");
        return abilities.get(index);
    }

    public boolean available(GameData game, UUID playerId, PlanarObject source, ActivatedAbility ability) {
        if (game.status != GameStatus.RUNNING || game.interaction.isAwaitingInput()
                || !Objects.equals(query.getPriorityPlayerId(game), playerId)
                || game.playersCantActivateAbilitiesThisTurn.contains(playerId)
                || (!ability.isManaAbility() && game.playersCantActivateNonManaAbilitiesThisTurn.contains(playerId))) return false;
        if (!ability.isActivatableByAnyPlayer() && !Objects.equals(game.planechase.controllerId, playerId)) return false;
        if (ability.isActivatableOnlyByOwner() && !Objects.equals(game.planechase.controllerId, playerId)) return false;
        if (game.pendingEffectResolutionEntry != null || !game.pendingMayAbilities.isEmpty()) return false;
        if (ability.isRequiresTap() || ability.isRequiresUntap()) return false;
        List<HandCardCost> handCosts = handCardCosts(ability);
        long costCount = ability.getEffects().stream().filter(CostEffect.class::isInstance).count();
        if (costCount != handCosts.size() || handCosts.size() > 1
                || handCosts.stream().anyMatch(cost -> !cost.exilesPaidCards())
                || ability.getLoyaltyCost() != null || ability.getMaxActivationsPerTurn() != null
                || ability.getMaxActivationsPerGame() != null || ability.getMaxActivationsPerTurnAmount() != null
                || ability.getActivationCondition() != null || ability.getRequiredControlledSubtype() != null
                || ability.getRequiredControlledPermanentPredicate() != null || ability.getRequiredGraveyardCardPredicate() != null
                || ability.isActivatableOnlyByEnchantedPermanentController() || ability.isRequiresAnotherActivatedAbility()
                || ability.isModalChoiceAtActivation() || !ability.getMultiTargetFilters().isEmpty()
                || ability.isRequiresXValue() || ability.getSourceCounterScaledTargetsType() != null) return false;
        int handSize = game.playerHands.getOrDefault(playerId, List.of()).size();
        if (handSize < ability.getMinCardsInHandToActivate()
                || (ability.getMaxCardsInHandToActivate() != null && handSize > ability.getMaxCardsInHandToActivate())) return false;
        if (ability.isActivatableOnlyByOpponents() && Objects.equals(game.planechase.controllerId, playerId)) return false;
        if (ability.isActivatableOnlyByGrantingPlayer() && !Objects.equals(ability.getGrantingPlayerId(), playerId)) return false;
        if (ability.getRequiredSourceCounterType() != null
                && source.getCounters().getOrDefault(ability.getRequiredSourceCounterType(), 0) < ability.getRequiredSourceCounterCount()) return false;
        if (!handCosts.isEmpty()) {
            HandCardCost handCost = handCosts.getFirst();
            int requiredCount = handCost.requiredCount(0);
            if (requiredCount > 0
                    && collectHandCardIndices(game, playerId, handCost, 0, null).size() < requiredCount) {
                return false;
            }
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED) {
            return Objects.equals(game.activePlayerId, playerId) && game.stack.isEmpty()
                    && (game.currentStep == TurnStep.PRECOMBAT_MAIN || game.currentStep == TurnStep.POSTCOMBAT_MAIN);
        }
        if (ability.getTimingRestriction() == ActivationTimingRestriction.ONLY_DURING_YOUR_TURN) {
            return Objects.equals(game.activePlayerId, playerId);
        }
        return ability.getTimingRestriction() == null;
    }

    public void activate(GameData game, UUID playerId, UUID sourceId, int index, int x, UUID targetId, Zone zone) {
        PlanarObject source = source(game, sourceId);
        ActivatedAbility ability = ability(source, index);
        if (x < 0 || !available(game, playerId, source, ability)) {
            throw new IllegalStateException("You cannot activate that planar ability now");
        }
        targeting.validateActivatedAbilityTargeting(game, playerId, ability, ability.getEffects(), targetId,
                zone, source.getCard(), x);
        ManaCost cost = new ManaCost(ability.getManaCost() == null ? "{0}" : ability.getManaCost());
        ManaPool pool = game.playerManaPools.get(playerId);
        ManaPool available = new ManaPool(pool);
        available.promoteAbilityOnlyMana();
        if (!cost.canPay(available, x, false, false, false, false, false,
                null, null, false, false, false, false, null, true)) {
            throw new IllegalStateException("Not enough mana");
        }
        pool.promoteAbilityOnlyMana();
        try {
            cost.pay(pool, x, false, false, false, false, false,
                    null, null, false, false, false, false, null, true);
        } finally {
            pool.restorePromotedAbilityOnlyMana();
        }
        List<HandCardCost> handCosts = handCardCosts(ability);
        if (!handCosts.isEmpty()) {
            HandCardCost handCost = handCosts.getFirst();
            List<Integer> validIndices = collectHandCardIndices(game, playerId, handCost, x, null);
            if (validIndices.size() < handCost.requiredCount(x)) {
                throw new IllegalStateException("No valid card to " + handCost.payVerb()
                        + " for the activation cost");
            }
            interactionHandlerRegistry.begin(game, new PendingInteraction.PlanarAbilityHandCardChoice(
                    playerId, sourceId, index, x, targetId, zone, validIndices,
                    planarHandCardPrompt(handCost)));
            return;
        }
        pushAbility(game, playerId, source, ability, x, targetId, zone, null);
    }

    /** Completes a planar activation after the player chooses the card for its hand cost. */
    public void completeHandCardCostChoice(GameData game, Player player,
                                            PendingInteraction.PlanarAbilityHandCardChoice choice,
                                            int cardIndex) {
        if (!player.getId().equals(choice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        PlanarObject source = source(game, choice.sourceId());
        ActivatedAbility ability = ability(source, choice.abilityIndex());
        HandCardCost handCost = handCardCosts(ability).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No hand-card cost on planar ability"));
        List<Integer> validIndices = collectHandCardIndices(game, player.getId(), handCost,
                choice.xValue(), null);
        if (!validIndices.contains(cardIndex)) {
            interactionHandlerRegistry.requestActiveDecision(game);
            return;
        }

        List<Card> hand = game.playerHands.get(player.getId());
        Card paidCard = hand.remove(cardIndex);
        if (handCost.imprintOnSource()) {
            game.setImprintedCard(source.getCard(), paidCard);
        }
        exileService.exileCard(game, player.getId(), paidCard);
        gameLogService.append(game, GameLog.textCardText(
                player.getUsername() + " exiles ", paidCard,
                " from their hand as an activation cost."));

        game.interaction.clearAwaitingInput();
        pushAbility(game, player.getId(), source, ability, choice.xValue(), choice.targetId(),
                choice.targetZone(), paidCard);
        stateBasedActionService.performStateBasedActions(game);
        mutationCoordinator.invalidateAllPlayerViews(game);
    }

    private void pushAbility(GameData game, UUID playerId, PlanarObject source, ActivatedAbility ability,
                             int xValue, UUID targetId, Zone zone, Card paidCard) {
        List<CardEffect> effects = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof CostEffect) {
                continue;
            }
            if (paidCard != null && effect instanceof ActivationCostCardReferenceEffect reference) {
                effects.add(reference.bindToCard(paidCard.getId(), paidCard.getManaValue()));
            } else {
                effects.add(effect);
            }
        }
        StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source.getCard(), playerId,
                ability.getDescription(), effects, targetId, zone, null);
        entry.setXValue(xValue);
        entry.setSourcePlanarObject(source.copy());
        entry.setTargetFilter(ability.getTargetFilter());
        game.priorityPassedBy.clear();
        game.revertableManaActivations.clear();
        if (ability.isManaAbility()) {
            game.manaAbilityResolutionDepth++;
            try {
                resolution.resolveEffects(game, entry);
            } finally {
                game.manaAbilityResolutionDepth--;
            }
        } else {
            game.stack.add(entry);
        }
    }

    private List<HandCardCost> handCardCosts(ActivatedAbility ability) {
        return ability.getEffects().stream()
                .filter(HandCardCost.class::isInstance)
                .map(HandCardCost.class::cast)
                .toList();
    }

    private List<Integer> collectHandCardIndices(GameData game, UUID playerId, HandCardCost cost,
                                                 int xValue, String requiredName) {
        List<Card> hand = game.playerHands.getOrDefault(playerId, List.of());
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (cost.manaValueEqualsX() && card.getManaValue() != xValue) {
                continue;
            }
            if (requiredName != null && !requiredName.equals(card.getName())) {
                continue;
            }
            if (!cost.isEligible(game, playerId, card)) {
                continue;
            }
            if (cost.predicate() == null || predicateEvaluationService.matchesCardPredicate(
                    card, cost.predicate(), null, game, playerId)) {
                validIndices.add(i);
            }
        }
        int requiredCount = cost.requiredCount(xValue);
        if (cost.sameName() && requiredName == null && requiredCount > 1) {
            Map<String, Long> countsByName = new HashMap<>();
            for (Integer index : validIndices) {
                countsByName.merge(hand.get(index).getName(), 1L, Long::sum);
            }
            validIndices.removeIf(index -> countsByName.get(hand.get(index).getName()) < requiredCount);
        }
        return validIndices;
    }

    private String planarHandCardPrompt(HandCardCost cost) {
        String label = cost.label() == null ? "" : cost.label() + " ";
        return "Choose a " + label + "card to " + cost.payVerb() + " as an activation cost.";
    }
}
