package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PsychicBattleSupport {

    private final TargetLegalityService targetLegalityService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    public StackEntry findTargetEntry(GameData gameData, UUID cardId) {
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry entry = gameData.stack.get(i);
            if (entry.getTargetableId().equals(cardId)) {
                return entry;
            }
        }
        // Legacy card-id reference: an ability sharing the spell's source card (a delayed copy
        // trigger, say) must not shadow the spell itself, so match the card only as a fallback.
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry entry = gameData.stack.get(i);
            if (entry.getCard() != null && entry.getCard().getId().equals(cardId)) {
                return entry;
            }
        }
        return null;
    }

    public List<UUID> targetIds(StackEntry entry) {
        List<UUID> ids = new ArrayList<>();
        if (entry.getTargetId() != null) {
            ids.add(entry.getTargetId());
        }
        if (!entry.getTargetIds().isEmpty()) {
            ids.addAll(entry.getDeclaredTargetIds());
        } else if (!entry.getTargetCardIds().isEmpty()) {
            ids.addAll(entry.getTargetCardIds());
        }
        return ids;
    }

    public List<UUID> collectLegalAlternatives(GameData gameData, StackEntry entry, int targetIndex) {
        List<UUID> chosen = targetIds(entry);
        if (targetIndex < 0 || targetIndex >= chosen.size()) {
            return List.of();
        }

        Set<UUID> candidates = collectCandidates(gameData, entry);
        List<UUID> valid = new ArrayList<>();
        for (UUID candidate : candidates) {
            if (chosen.get(targetIndex).equals(candidate)) {
                continue;
            }
            if (isLegalReplacement(gameData, entry, targetIndex, candidate)) {
                valid.add(candidate);
            }
        }
        return valid;
    }

    public boolean queueNextChoice(GameData gameData, Card sourceCard, UUID controllerId,
                                   UUID spellCardId, int targetIndex) {
        StackEntry entry = findTargetEntry(gameData, spellCardId);
        if (entry == null || entry.isNonTargeting()) {
            return false;
        }

        List<UUID> targets = targetIds(entry);
        for (int index = Math.max(0, targetIndex); index < targets.size(); index++) {
            if (!collectLegalAlternatives(gameData, entry, index).isEmpty()) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        sourceCard,
                        controllerId,
                        List.of(new PsychicBattleRetargetEffect(spellCardId, index)),
                        "Change the target of " + entry.getCard().getName() + "?"
                ));
                return true;
            }
        }
        return false;
    }

    public void replaceTarget(StackEntry entry, int targetIndex, UUID targetId) {
        if (!entry.getDamageAssignments().isEmpty()) {
            UUID original = targetIds(entry).get(targetIndex);
            Integer amount = entry.getDamageAssignments().remove(original);
            if (amount != null) entry.getDamageAssignments().put(targetId, amount);
        }
        if (entry.getTargetId() != null) {
            if (targetIndex == 0) {
                entry.setTargetId(targetId);
                return;
            }
            int flatIndex = targetIndex - 1;
            if (!entry.getTargetIds().isEmpty()) {
                entry.replaceTargetIdAt(flatIndex, targetId);
            } else {
                entry.replaceTargetCardIdAt(flatIndex, targetId);
            }
            return;
        }
        if (!entry.getTargetIds().isEmpty()) {
            entry.replaceTargetIdAt(targetIndex, targetId);
        } else {
            entry.replaceTargetCardIdAt(targetIndex, targetId);
        }
    }

    public void beginPermanentChoice(GameData gameData, Card sourceCard, UUID controllerId,
                                     UUID spellCardId, int targetIndex) {
        StackEntry entry = findTargetEntry(gameData, spellCardId);
        if (entry == null) {
            return;
        }
        List<UUID> validTargets = collectLegalAlternatives(gameData, entry, targetIndex);
        if (validTargets.isEmpty()) {
            return;
        }
        gameData.interaction.setPermanentChoiceContext(
                new com.github.laxika.magicalvibes.model.PermanentChoiceContext.PsychicBattleRetarget(
                        spellCardId, controllerId, sourceCard, targetIndex));
        playerInputService.beginAnyTargetChoice(gameData, controllerId,
                validTargets.stream().filter(id -> !gameData.playerIds.contains(id)).toList(),
                validTargets.stream().filter(gameData.playerIds::contains).toList(),
                "Choose a new target for " + entry.getCard().getName() + ".");
    }

    /**
     * Starts the next step of a "choose new targets" sequence (CR 115.7d) at the first target position
     * at or after {@code fromIndex} that has a legal replacement. The chooser may keep the current target
     * (it is offered alongside the legal replacements) or pick a replacement; the choice is then handled by
     * the {@code SpellRetarget} context, which continues with the following target.
     *
     * @return whether a choice was started; false when no remaining position can be changed
     */
    public boolean beginEachTargetChoice(GameData gameData, StackEntry entry, UUID chooserId, int fromIndex) {
        List<UUID> current = targetIds(entry);
        for (int index = Math.max(0, fromIndex); index < current.size(); index++) {
            List<UUID> alternatives = collectLegalAlternatives(gameData, entry, index);
            if (alternatives.isEmpty()) {
                continue;
            }
            List<UUID> choices = new ArrayList<>();
            choices.add(current.get(index));
            choices.addAll(alternatives);
            gameData.interaction.setPermanentChoiceContext(
                    new com.github.laxika.magicalvibes.model.PermanentChoiceContext.SpellRetarget(
                            entry.getTargetableId(), index, null, chooserId, true));
            playerInputService.beginAnyTargetChoice(gameData, chooserId,
                    choices.stream().filter(id -> !gameData.playerIds.contains(id)).toList(),
                    choices.stream().filter(gameData.playerIds::contains).toList(),
                    "Choose the new target for target " + (index + 1) + " of " + entry.getCard().getName()
                            + " (choose the current target to leave it unchanged).");
            return true;
        }
        return false;
    }

    private Set<UUID> collectCandidates(GameData gameData, StackEntry entry) {
        Set<UUID> candidates = new LinkedHashSet<>();
        if (entry.getTargetZone() == Zone.STACK) {
            for (StackEntry stackEntry : gameData.stack) {
                if (!stackEntry.getTargetableId().equals(entry.getTargetableId())) {
                    candidates.add(stackEntry.getTargetableId());
                }
            }
            StackEntry resolving = gameData.pendingEffectResolutionEntry;
            if (resolving != null && !resolving.getTargetableId().equals(entry.getTargetableId())) {
                candidates.add(resolving.getTargetableId());
            }
        } else if (entry.getTargetZone() == Zone.GRAVEYARD) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                gameData.playerGraveyards.getOrDefault(playerId, List.of())
                        .forEach(card -> candidates.add(card.getId()));
            }
        } else {
            gameData.forEachPermanent((playerId, permanent) -> candidates.add(permanent.getId()));
            candidates.addAll(gameData.orderedPlayerIds);
        }
        return candidates;
    }

    private boolean isLegalReplacement(GameData gameData, StackEntry entry, int targetIndex, UUID candidate) {
        if (!entry.getDamageAssignments().isEmpty()) {
            var divided = entry.getEffectsToResolve().stream()
                    .filter(com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect.class::isInstance)
                    .map(com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect.class::cast).findFirst().orElse(null);
            if (divided != null) {
                var targetedDamage = new com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect(
                        divided.totalDamage(), divided.orderedAmounts(), divided.mode(), divided.targetRestriction(),
                        divided.maxTargets(), divided.canTargetPlayers(), divided.damagedCreaturesCantBlock(), false,
                        divided.tapDamagedCreatures(), divided.damagedPlayersCantCastNoncreatureSpells(),
                        divided.canTargetPlaneswalkers());
                List<CardEffect> effects = List.of(targetedDamage);
                var ability = new com.github.laxika.magicalvibes.model.ActivatedAbility(false, null,
                        effects, "retarget", entry.getTargetFilter());
                try {
                    targetLegalityService.validateActivatedAbilityTargeting(gameData, entry.getControllerId(),
                            ability, effects, candidate, entry.getTargetZone(), entry.getCard(), entry.getXValue());
                    return true;
                } catch (IllegalStateException ignored) {
                    return false;
                }
            }
        }
        if (entry.getEntryType() == com.github.laxika.magicalvibes.model.StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY) {
            var ability = new com.github.laxika.magicalvibes.model.ActivatedAbility(false, null,
                    List.copyOf(entry.getEffectsToResolve()), "retarget", entry.getTargetFilter());
            try {
                targetLegalityService.validateActivatedAbilityTargeting(gameData, entry.getControllerId(),
                        ability, entry.getEffectsToResolve(), candidate, entry.getTargetZone(),
                        entry.getCard(), entry.getXValue());
                return true;
            } catch (IllegalStateException ignored) {
                return false;
            }
        }
        if (!entry.getTargetIds().isEmpty()
                && entry.getTargetId() == null
                && !entry.getCard().getSpellTargets().isEmpty()) {
            List<UUID> replacement = new ArrayList<>(entry.getDeclaredTargetIds());
            replacement.set(targetIndex, candidate);
            try {
                if (entry.getTargetZone() == Zone.STACK) {
                    targetLegalityService.validateMultiSpellTargetsOnStack(
                            gameData, entry.getCard(), replacement, entry.getControllerId(), entry.isKicked());
                } else {
                    targetLegalityService.validateMultiSpellTargets(
                            gameData, entry.getCard(), replacement, entry.getControllerId(), entry.getXValue(),
                            entry.isKicked());
                }
                return true;
            } catch (IllegalStateException ignored) {
                return false;
            }
        }

        if (entry.getTargetZone() == Zone.STACK) {
            return targetLegalityService.checkSpellTargetOnStack(
                    gameData, candidate, entry.getTargetFilter() != null
                            ? entry.getTargetFilter() : entry.getCard().getTargetFilter(),
                    entry.getControllerId()).isEmpty();
        }
        if (entry.getTargetZone() == Zone.GRAVEYARD) {
            return targetLegalityService.checkGraveyardRetargetCandidate(
                    gameData, entry.getCard(), candidate, entry.getControllerId()).isEmpty();
        }
        if (entry.getTargetFilter() != null) {
            var permanent = gameQueryService.findPermanentById(gameData, candidate);
            if (permanent == null && entry.getTargetFilter() instanceof PermanentPredicateTargetFilter) {
                return false;
            }
            if (permanent != null && predicateEvaluationService.checkTargetFilter(
                    entry.getTargetFilter(), permanent, FilterContext.of(gameData)).isPresent()) {
                return false;
            }
        }
        return targetLegalityService.checkSpellTargeting(
                gameData, entry.getCard(), candidate, null, entry.getControllerId()).isEmpty();
    }
}
