package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
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
    private void resolveChangeTargetOfTargetSpellWithSingleTarget(GameData gameData, StackEntry entry) {
        StackEntry targetSpell = findStackEntryByCardId(gameData, entry.getTargetPermanentId());
        if (targetSpell == null) {
            return;
        }

        if (!isSingleTargetSpell(targetSpell)) {
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
    private void resolveChangeTargetOfTargetSpellToSource(GameData gameData, StackEntry entry) {
        StackEntry targetSpell = findStackEntryByCardId(gameData, entry.getTargetPermanentId());
        if (targetSpell == null) {
            return;
        }

        if (!hasAnyTarget(targetSpell)) {
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

        if (isSingleTargetSpell(targetSpell)) {
            if (sourcePermanentId.equals(targetSpell.getTargetPermanentId())) {
                String logEntry = targetSpell.getCard().getName() + " already targets " + entry.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                return;
            }
            if (isValidNewTargetForSpell(gameData, targetSpell, sourcePermanentId)) {
                targetSpell.setTargetPermanentId(sourcePermanentId);
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

    private boolean hasAnyTarget(StackEntry stackEntry) {
        return stackEntry.getTargetPermanentId() != null
                || !stackEntry.getTargetPermanentIds().isEmpty()
                || !stackEntry.getTargetCardIds().isEmpty();
    }

    private List<UUID> collectValidNewTargets(GameData gameData, StackEntry targetSpell) {
        UUID currentTargetId = targetSpell.getTargetPermanentId();
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

        try {
            if (spellCard.isNeedsSpellTarget()) {
                targetLegalityService.validateSpellTargetOnStack(gameData, candidateTargetId, spellCard.getTargetFilter(), targetSpell.getControllerId());
                return true;
            }

            if (targetSpell.getTargetZone() == Zone.GRAVEYARD) {
                if (gameQueryService.findCardInGraveyardById(gameData, candidateTargetId) == null) {
                    return false;
                }
                ReturnCardFromGraveyardEffect graveyardEffect = (ReturnCardFromGraveyardEffect) spellCard.getEffects(EffectSlot.SPELL)
                        .stream()
                        .filter(e -> e instanceof ReturnCardFromGraveyardEffect)
                        .findFirst().orElse(null);
                if (graveyardEffect != null && graveyardEffect.source() == GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
                    boolean inControllersGraveyard = gameData.playerGraveyards
                            .getOrDefault(targetSpell.getControllerId(), List.of())
                            .stream()
                            .anyMatch(c -> c.getId().equals(candidateTargetId));
                    if (!inControllersGraveyard) {
                        return false;
                    }
                }
                targetLegalityService.validateEffectTargetInZone(gameData, spellCard, candidateTargetId, Zone.GRAVEYARD);
                return true;
            }

            targetLegalityService.validateSpellTargeting(
                    gameData,
                    spellCard,
                    candidateTargetId,
                    null,
                    targetSpell.getControllerId()
            );
            return true;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    private StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) return null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(cardId)) {
                return se;
            }
        }
        return null;
    }

    private boolean isSingleTargetSpell(StackEntry stackEntry) {
        return stackEntry.getTargetPermanentId() != null
                && stackEntry.getTargetPermanentIds().isEmpty()
                && stackEntry.getTargetCardIds().isEmpty();
    }
}


