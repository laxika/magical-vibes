package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleRetargetEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
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

    public StackEntry findTargetEntry(GameData gameData, UUID cardId) {
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry entry = gameData.stack.get(i);
            if (entry.getCard().getId().equals(cardId)) {
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
            if (chosen.contains(candidate)) {
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

    private Set<UUID> collectCandidates(GameData gameData, StackEntry entry) {
        Set<UUID> candidates = new LinkedHashSet<>();
        if (entry.getTargetZone() == Zone.STACK) {
            for (StackEntry stackEntry : gameData.stack) {
                if (!stackEntry.getCard().getId().equals(entry.getCard().getId())) {
                    candidates.add(stackEntry.getCard().getId());
                }
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
        if (!entry.getTargetIds().isEmpty()
                && entry.getTargetId() == null
                && !entry.getCard().getSpellTargets().isEmpty()) {
            List<UUID> replacement = new ArrayList<>(entry.getDeclaredTargetIds());
            replacement.set(targetIndex, candidate);
            try {
                if (entry.getTargetZone() == Zone.STACK) {
                    targetLegalityService.validateMultiSpellTargetsOnStack(
                            gameData, entry.getCard(), replacement, entry.getControllerId());
                } else {
                    targetLegalityService.validateMultiSpellTargets(
                            gameData, entry.getCard(), replacement, entry.getControllerId(), entry.getXValue());
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
        return targetLegalityService.checkSpellTargeting(
                gameData, entry.getCard(), candidate, null, entry.getControllerId()).isEmpty();
    }
}
