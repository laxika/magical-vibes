package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Phasing (CR 702.26), the first turn-based action of the untap step (CR 502.1): before the active
 * player untaps, every phased-in permanent with phasing that they control phases out, and every
 * phased-out permanent that phased out under their control phases in — simultaneously.
 *
 * <p>A phased-out permanent "is treated as though it does not exist" (CR 702.26b), so instead of
 * flagging it in place this service moves the {@code Permanent} object off its battlefield and into
 * {@link GameData#phasedOutPermanents}. Every battlefield query, continuous effect, state-based
 * action and combat check therefore ignores it for free. Phasing is not a zone change
 * (CR 702.26d): the same object comes back with its counters, damage, attachments and CR 613.7
 * timestamp intact, and no enter/leave-the-battlefield trigger fires either way.
 *
 * <p>Auras and Equipment attached to a permanent that phases out phase out "indirectly" alongside
 * it (CR 702.26g) and never phase in on their own — they come back with their host, even when a
 * different player controls them. That is what {@link Permanent#isPhasedOutIndirectly()} records.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhasingService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    /**
     * Runs the untap step's phasing turn-based action for {@code activePlayerId}. Both directions
     * are computed before anything moves, so they happen simultaneously (CR 502.1).
     *
     * @param gameData       the current game state to modify
     * @param activePlayerId the player whose untap step is being processed
     */
    public void applyPhasing(GameData gameData, UUID activePlayerId) {
        Map<Permanent, UUID> phasingIn = collectPhasingIn(gameData, activePlayerId);
        Set<Permanent> phasingOut = collectPhasingOut(gameData, activePlayerId);
        if (phasingIn.isEmpty() && phasingOut.isEmpty()) {
            return;
        }

        phasingIn.forEach((permanent, controllerId) -> {
            phasedOutList(gameData, controllerId).remove(permanent);
            permanent.setPhasedOutIndirectly(false);
            gameData.playerBattlefields
                    .computeIfAbsent(controllerId, id -> gameData.newBattlefieldList())
                    .add(permanent);
            triggerCollectionService.checkPhasesInTriggers(gameData, permanent, controllerId);
        });

        movePhasedOut(gameData, phasingOut);

        logPhasing(gameData, activePlayerId, phasingIn.keySet(), phasingOut);
    }

    /**
     * Phases out the given permanents right now, outside the untap step's turn-based action — for
     * effects that make something phase out on resolution (e.g. Dream Fighter). Everything attached
     * to them phases out indirectly alongside (CR 702.26g), and each phased-out permanent is removed
     * from combat (CR 506.4). Because they phase out directly, they phase in during their
     * controller's next untap step even without the phasing keyword (CR 702.26a).
     *
     * @param gameData   the current game state to modify
     * @param permanents the permanents to phase out; those no longer on a battlefield are ignored
     */
    public void phaseOut(GameData gameData, Collection<Permanent> permanents) {
        Set<Permanent> phasingOut = new LinkedHashSet<>();
        Deque<Permanent> pending = new ArrayDeque<>();
        permanents.stream()
                .filter(permanent -> controllerOf(gameData, permanent) != null)
                .filter(PhasingService::canPhaseOut)
                .forEach(permanent -> {
                    permanent.setPhasedOutIndirectly(false);
                    if (phasingOut.add(permanent)) {
                        pending.add(permanent);
                    }
                });
        expandAttachments(gameData, pending, phasingOut);
        if (phasingOut.isEmpty()) {
            return;
        }

        movePhasedOut(gameData, phasingOut);

        String names = names(phasingOut);
        gameLogService.append(gameData, GameLog.text(names + " phases out."));
        log.info("Game {} - {} phases out", gameData.id, names);
    }

    /**
     * Moves each permanent off its controller's battlefield into {@link GameData#phasedOutPermanents},
     * clearing its combat state on the way out (CR 506.4 — a permanent that phases out is removed
     * from combat).
     */
    private void movePhasedOut(GameData gameData, Set<Permanent> phasingOut) {
        phasingOut.forEach(permanent -> {
            UUID controllerId = controllerOf(gameData, permanent);
            if (controllerId == null) {
                return;
            }
            triggerCollectionService.checkPhasesOutTriggers(gameData, permanent, controllerId);
            permanent.clearCombatState();
            gameData.playerBattlefields.get(controllerId).remove(permanent);
            phasedOutList(gameData, controllerId).add(permanent);
        });
    }

    /**
     * The permanents that phase in this untap step, mapped to the player who controlled them when
     * they phased out (they return to that player's battlefield — phasing never changes control).
     * Only permanents that phased out <em>directly</em> under the active player's control phase in,
     * dragging everything attached to them along (CR 702.26g).
     */
    private Map<Permanent, UUID> collectPhasingIn(GameData gameData, UUID activePlayerId) {
        Map<Permanent, UUID> phasingIn = new LinkedHashMap<>();
        Deque<Permanent> pending = new ArrayDeque<>();
        phasedOutList(gameData, activePlayerId).stream()
                .filter(permanent -> !permanent.isPhasedOutIndirectly())
                .forEach(permanent -> {
                    phasingIn.put(permanent, activePlayerId);
                    pending.add(permanent);
                });

        while (!pending.isEmpty()) {
            Permanent host = pending.poll();
            gameData.phasedOutPermanents.forEach((controllerId, permanents) -> permanents.stream()
                    .filter(permanent -> host.getId().equals(permanent.getAttachedTo()))
                    .filter(permanent -> !phasingIn.containsKey(permanent))
                    .forEach(permanent -> {
                        phasingIn.put(permanent, controllerId);
                        pending.add(permanent);
                    }));
        }
        return phasingIn;
    }

    /**
     * The permanents that phase out this untap step: the active player's phased-in permanents with
     * phasing, plus everything attached to them, however deep the attachment chain runs. An object
     * that would phase out both directly and indirectly just phases out indirectly (CR 702.26h),
     * so the indirect flag is set last and wins.
     */
    private Set<Permanent> collectPhasingOut(GameData gameData, UUID activePlayerId) {
        Set<Permanent> phasingOut = new LinkedHashSet<>();
        Deque<Permanent> pending = new ArrayDeque<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield != null) {
            List.copyOf(battlefield).stream()
                    .filter(permanent -> gameQueryService.hasKeyword(gameData, permanent, Keyword.PHASING))
                    .filter(PhasingService::canPhaseOut)
                    .forEach(permanent -> {
                        permanent.setPhasedOutIndirectly(false);
                        phasingOut.add(permanent);
                        pending.add(permanent);
                    });
        }

        expandAttachments(gameData, pending, phasingOut);
        return phasingOut;
    }

    /**
     * Walks the attachment chain down from every permanent in {@code pending}, adding each
     * Aura/Equipment attached to it (however deep) to {@code phasingOut} as an indirect phase-out
     * (CR 702.26g). An object that would phase out both directly and indirectly just phases out
     * indirectly (CR 702.26h), so the indirect flag is set last and wins.
     */
    private void expandAttachments(GameData gameData, Deque<Permanent> pending, Set<Permanent> phasingOut) {
        while (!pending.isEmpty()) {
            Permanent host = pending.poll();
            gameData.forEachBattlefield((controllerId, permanents) -> List.copyOf(permanents).stream()
                    .filter(permanent -> host.getId().equals(permanent.getAttachedTo()))
                    .filter(PhasingService::canPhaseOut)
                    .filter(permanent -> !phasingOut.contains(permanent) || !permanent.isPhasedOutIndirectly())
                    .forEach(permanent -> {
                        permanent.setPhasedOutIndirectly(true);
                        if (phasingOut.add(permanent)) {
                            pending.add(permanent);
                        }
                    }));
        }
    }

    /**
     * Whether {@code permanent} is currently allowed to phase out. Spatial Binding's "target permanent
     * can't phase out" marks it with the protecting player's id until that player's next upkeep; a
     * marked permanent is skipped by every phasing pass, direct and indirect (CR 702.26g) alike.
     */
    private static boolean canPhaseOut(Permanent permanent) {
        return permanent.getCantPhaseOutUntilUpkeepOf() == null;
    }

    private UUID controllerOf(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    private List<Permanent> phasedOutList(GameData gameData, UUID controllerId) {
        return gameData.phasedOutPermanents.computeIfAbsent(controllerId,
                id -> Collections.synchronizedList(new ArrayList<>()));
    }

    private void logPhasing(GameData gameData, UUID activePlayerId,
                            Set<Permanent> phasedIn, Set<Permanent> phasedOut) {
        String playerName = gameData.playerIdToName.get(activePlayerId);
        if (!phasedOut.isEmpty()) {
            String names = names(phasedOut);
            gameLogService.append(gameData, GameLog.text(names + " phases out."));
            log.info("Game {} - {} phases out during {}'s untap step", gameData.id, names, playerName);
        }
        if (!phasedIn.isEmpty()) {
            String names = names(phasedIn);
            gameLogService.append(gameData, GameLog.text(names + " phases in."));
            log.info("Game {} - {} phases in during {}'s untap step", gameData.id, names, playerName);
        }
    }

    private String names(Set<Permanent> permanents) {
        return permanents.stream().map(permanent -> permanent.getCard().getName()).reduce(
                (left, right) -> left + ", " + right).orElse("");
    }
}
