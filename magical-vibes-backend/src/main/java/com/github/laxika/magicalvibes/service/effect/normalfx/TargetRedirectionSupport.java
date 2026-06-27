package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
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

    private final GameQueryService gameQueryService;
    private final TargetLegalityService targetLegalityService;

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
        Card spellCard = targetSpell.getCard();

        if (EffectResolution.needsSpellTarget(spellCard)) {
            return targetLegalityService.checkSpellTargetOnStack(gameData, candidateTargetId, spellCard.getTargetFilter(), targetSpell.getControllerId()).isEmpty();
        }

        if (targetSpell.getTargetZone() == Zone.GRAVEYARD) {
            return targetLegalityService.checkGraveyardRetargetCandidate(gameData, spellCard, candidateTargetId, targetSpell.getControllerId()).isEmpty();
        }

        return targetLegalityService.checkSpellTargeting(gameData, spellCard, candidateTargetId, null, targetSpell.getControllerId()).isEmpty();
    }
}
