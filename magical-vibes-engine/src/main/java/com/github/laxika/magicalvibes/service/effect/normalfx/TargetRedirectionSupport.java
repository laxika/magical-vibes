package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared target-redirection helpers used by every "normal" retarget effect handler.
 *
 * <p>Extracted verbatim from {@code TargetRedirectionResolutionService}; behavior is identical.
 */
@Component
@RequiredArgsConstructor
public class TargetRedirectionSupport {

    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;

    public List<UUID> collectValidNewTargets(GameData gameData, StackEntry targetSpell) {
        UUID currentTargetId = targetSpell.getTargetId();
        List<UUID> candidates = new ArrayList<>();

        if (targetSpell.getTargetZone() == Zone.STACK) {
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(targetSpell.getCard().getId())) continue;
                candidates.add(se.getCard().getId());
            }
        } else if (targetSpell.getTargetZone() == Zone.GRAVEYARD) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                gameData.playerGraveyards.getOrDefault(playerId, List.of())
                        .forEach(card -> candidates.add(card.getId()));
            }
        } else {
            gameData.forEachPermanent((playerId, permanent) -> candidates.add(permanent.getId()));
            candidates.addAll(gameData.orderedPlayerIds);
        }

        List<UUID> validTargets = new ArrayList<>();
        for (UUID candidate : candidates) {
            if (candidate.equals(currentTargetId)) {
                continue;
            }
            if (isValidNewTargetForSpell(gameData, targetSpell, candidate)) {
                validTargets.add(candidate);
            }
        }
        return validTargets;
    }

    public boolean isValidNewTargetForSpell(GameData gameData, StackEntry targetSpell, UUID candidateTargetId) {
        if (targetSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || targetSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            return isValidNewTargetForAbility(gameData, targetSpell, candidateTargetId);
        }

        Card spellCard = targetSpell.getCard();

        if (EffectResolution.needsSpellTarget(spellCard)) {
            return targetLegalityService.checkSpellTargetOnStack(gameData, candidateTargetId, spellCard.getTargetFilter(), targetSpell.getControllerId()).isEmpty();
        }

        if (targetSpell.getTargetZone() == Zone.GRAVEYARD) {
            return targetLegalityService.checkGraveyardRetargetCandidate(gameData, spellCard, candidateTargetId, targetSpell.getControllerId()).isEmpty();
        }

        return targetLegalityService.checkSpellTargeting(gameData, spellCard, candidateTargetId, null, targetSpell.getControllerId()).isEmpty();
    }

    private boolean isValidNewTargetForAbility(GameData gameData, StackEntry abilityEntry, UUID candidateTargetId) {
        List<CardEffect> effects = abilityEntry.getEffectsToResolve() == null
                ? List.of() : abilityEntry.getEffectsToResolve();
        ActivatedAbility syntheticAbility = new ActivatedAbility(
                false, null, effects, "retarget", abilityEntry.getTargetFilter());
        int sourcePermanentIndex = findPermanentIndex(gameData, abilityEntry.getSourcePermanentId());
        ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForAbility(
                gameData, abilityEntry.getCard(), syntheticAbility,
                abilityEntry.getControllerId(), sourcePermanentIndex);
        return validTargets.validPermanentIds().contains(candidateTargetId)
                || validTargets.validPlayerIds().contains(candidateTargetId)
                || validTargets.validGraveyardCardIds().contains(candidateTargetId);
    }

    private int findPermanentIndex(GameData gameData, UUID permanentId) {
        if (permanentId == null) return -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (int i = 0; i < battlefield.size(); i++) {
                if (permanentId.equals(battlefield.get(i).getId())) return i;
            }
        }
        return -1;
    }
}
