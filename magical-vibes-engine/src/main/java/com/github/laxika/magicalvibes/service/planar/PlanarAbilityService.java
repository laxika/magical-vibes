package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlanarAbilityService {
    private final GameQueryService query;
    private final TargetLegalityService targeting;
    private final EffectResolutionService resolution;

    public PlanarAbilityService(@Lazy GameQueryService query, @Lazy TargetLegalityService targeting,
                                @Lazy EffectResolutionService resolution) {
        this.query = query;
        this.targeting = targeting;
        this.resolution = resolution;
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
        if (ability.getEffects().stream().anyMatch(CostEffect.class::isInstance)
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
        StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source.getCard(), playerId,
                ability.getDescription(), new ArrayList<>(ability.getEffects()), targetId, zone, null);
        entry.setXValue(x);
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
}
