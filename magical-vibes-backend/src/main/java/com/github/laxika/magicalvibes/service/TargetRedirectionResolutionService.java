package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TargetRedirectionResolutionService implements EffectHandlerProvider {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TargetLegalityService targetLegalityService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(ChangeTargetOfTargetSpellWithSingleTargetEffect.class,
                (gd, entry, effect) -> resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry));
    }

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

    private List<UUID> collectValidNewTargets(GameData gameData, StackEntry targetSpell) {
        UUID currentTargetId = targetSpell.getTargetPermanentId();
        List<UUID> candidates = new ArrayList<>();

        if (targetSpell.getTargetZone() == TargetZone.STACK) {
            for (StackEntry se : gameData.stack) {
                if (se.getCard().getId().equals(targetSpell.getCard().getId())) continue;
                candidates.add(se.getCard().getId());
            }
        } else if (targetSpell.getTargetZone() == TargetZone.GRAVEYARD) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                gameData.playerGraveyards.getOrDefault(playerId, List.of())
                        .forEach(card -> candidates.add(card.getId()));
            }
        } else {
            for (UUID playerId : gameData.orderedPlayerIds) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    candidates.add(permanent.getId());
                }
            }
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
                targetLegalityService.validateSpellTargetOnStack(gameData, candidateTargetId, spellCard.getTargetFilter());
                return true;
            }

            if (targetSpell.getTargetZone() == TargetZone.GRAVEYARD) {
                if (gameQueryService.findCardInGraveyardById(gameData, candidateTargetId) == null) {
                    return false;
                }
                targetLegalityService.validateEffectTargetInZone(gameData, spellCard, candidateTargetId, TargetZone.GRAVEYARD);
                return true;
            }

            targetLegalityService.validateSpellTargeting(gameData, spellCard, candidateTargetId, null);
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


