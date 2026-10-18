package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared prevention/shield helpers used by every "normal" Prevention effect handler.
 *
 * <p>Extracted verbatim from {@code PreventionResolutionService}; behavior is identical.
 */
@Component
@RequiredArgsConstructor
public class PreventionSupport {

    private final GameLogService gameLogService;

    public List<UUID> collectAllBattlefieldPermanentIds(GameData gameData) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> validIds.add(perm.getId()));
        return validIds;
    }

    public List<UUID> collectAllDamageSourceIds(GameData gameData) {
        List<UUID> validIds = collectAllBattlefieldPermanentIds(gameData);
        gameData.stack.stream()
                .filter(entry -> entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                .map(entry -> entry.getCard().getId())
                .forEach(validIds::add);
        // An ability whose source is a card outside the battlefield (a "when you discard this card"
        // trigger) is itself a source of damage: its card is the source.
        gameData.stack.stream()
                .filter(entry -> (entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                        || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                        && entry.getSourcePermanentId() == null
                        && entry.getCard() != null)
                .map(entry -> entry.getCard().getId())
                .filter(id -> !validIds.contains(id))
                .forEach(validIds::add);
        gameData.stack.stream()
                .filter(entry -> entry.getSourcePermanentId() != null
                        && entry.getSourcePermanentSnapshot() != null)
                .map(entry -> entry.getSourcePermanentId())
                .filter(id -> !validIds.contains(id))
                .forEach(validIds::add);
        // CR 609.7a: an object referred to by a waiting replacement or prevention effect can be chosen as a
        // source even after it has left the battlefield.
        gameData.creatureDamageRedirectShields.stream()
                .map(shield -> shield.damageSourceId())
                .filter(id -> !validIds.contains(id))
                .forEach(validIds::add);
        gameData.playerSourceNextDamageRedirectShields.stream()
                .map(shield -> shield.sourcePermanentId())
                .filter(id -> !validIds.contains(id))
                .forEach(validIds::add);
        return validIds;
    }

    public void broadcastNoPermanentsForDamageSourceChoice(GameData gameData) {
        String logEntry = "No permanents on the battlefield to choose as a damage source.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }

    public void broadcastNoDamageSourcesForChoice(GameData gameData) {
        gameLogService.append(gameData, GameLog.text("No damage sources to choose from."));
    }
}
