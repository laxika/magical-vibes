package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetRedirectionResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TargetLegalityService targetLegalityService;

    @HandlesEffect(ChangeTargetOfTargetSpellWithSingleTargetEffect.class)
    void resolveChangeTargetOfTargetSpellWithSingleTarget(GameData gameData, StackEntry entry) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        if (!targetSpell.isSingleTarget()) {
            String logEntry = entry.getCard().getName() + " has no effect (" + targetSpell.getCard().getName() + " no longer has a single target).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<UUID> validNewTargets = collectValidNewTargets(gameData, targetSpell);
        if (validNewTargets.isEmpty()) {
            String logEntry = "No legal new target for " + targetSpell.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SpellRetarget(targetSpell.getCard().getId()));
        playerInputService.beginPermanentChoice(
                gameData,
                entry.getControllerId(),
                validNewTargets,
                "Choose a new target for " + targetSpell.getCard().getName() + "."
        );
    }

    @HandlesEffect(ChangeTargetOfTargetSpellToSourceEffect.class)
    void resolveChangeTargetOfTargetSpellToSource(GameData gameData, StackEntry entry) {
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        if (!targetSpell.hasAnyTarget()) {
            String logEntry = entry.getCard().getName() + " has no effect (" + targetSpell.getCard().getName() + " has no targets).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            String logEntry = entry.getCard().getName() + " has no effect (source permanent no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        if (targetSpell.isSingleTarget()) {
            if (sourcePermanentId.equals(targetSpell.getTargetId())) {
                String logEntry = targetSpell.getCard().getName() + " already targets " + entry.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }
            if (isValidNewTargetForSpell(gameData, targetSpell, sourcePermanentId)) {
                targetSpell.setTargetId(sourcePermanentId);
                String logEntry = targetSpell.getCard().getName() + "'s target is changed to " + entry.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else {
                String logEntry = entry.getCard().getName() + " is not a legal target for " + targetSpell.getCard().getName() + ". Target not changed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        } else {
            String logEntry = entry.getCard().getName() + " has no effect (" + targetSpell.getCard().getName() + " does not have a single target).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }
    }

    private List<UUID> collectValidNewTargets(GameData gameData, StackEntry targetSpell) {
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

    private boolean isValidNewTargetForSpell(GameData gameData, StackEntry targetSpell, UUID candidateTargetId) {
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
