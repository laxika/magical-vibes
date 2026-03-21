package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the untap step (CR 502): cleaning stale untap-prevention locks,
 * untapping the active player's permanents (respecting "doesn't untap" effects),
 * queuing may-not-untap choices, clearing summoning sickness, and handling
 * "untap during each other player's untap step" effects (e.g. Seedborn Muse).
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate untap-step logic
 * into a focused service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UntapStepService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    /**
     * Performs the untap step for the active player.
     *
     * <ol>
     *   <li>Removes stale untap-prevention locks whose source permanent has left
     *       the battlefield or is no longer tapped.</li>
     *   <li>Untaps each of the active player's permanents unless it has a
     *       {@link DoesntUntapDuringUntapStepEffect}, an attached
     *       {@link AttachedCreatureDoesntUntapEffect}, or an active untap lock.</li>
     *   <li>Queues a {@code PendingMayAbility} for each tapped permanent with
     *       {@link MayNotUntapDuringUntapStepEffect}, letting the controller choose.</li>
     *   <li>Clears summoning sickness and loyalty-ability-used flags.</li>
     *   <li>Untaps non-active players' permanents that have an
     *       {@link UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect}
     *       (e.g. Seedborn Muse).</li>
     * </ol>
     *
     * @param gameData       the current game state to modify
     * @param activePlayerId the player whose untap step is being processed
     */
    public void untapPermanents(GameData gameData, UUID activePlayerId) {
        String activePlayerName = gameData.playerIdToName.get(activePlayerId);

        // Clean up stale untap-prevention locks on ALL battlefields before untapping.
        // A lock is stale if the source permanent is no longer on the battlefield or is no longer tapped.
        gameData.forEachPermanent((pid, p) -> {
            if (p.getUntapPreventedByPermanentIds().isEmpty()) return;
            p.getUntapPreventedByPermanentIds().removeIf(sourceId -> {
                Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
                return source == null || !source.isTapped();
            });
        });

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> mayNotUntapPermanents = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                boolean hasAttachedDoesntUntap = gameQueryService.hasAuraWithEffect(gameData, p, AttachedCreatureDoesntUntapEffect.class);
                boolean hasSelfDoesntUntap = p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DoesntUntapDuringUntapStepEffect);
                boolean hasMayNotUntap = p.isTapped() && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
                boolean hasUntapLock = !p.getUntapPreventedByPermanentIds().isEmpty();
                boolean skipsNextUntap = p.getSkipUntapCount() > 0;

                if (skipsNextUntap) {
                    // Decrement skip counter but don't untap this step (e.g. Vorinclex)
                    p.setSkipUntapCount(p.getSkipUntapCount() - 1);
                } else if (hasMayNotUntap) {
                    // Present choice to controller later — skip untap for now
                    mayNotUntapPermanents.add(p);
                } else if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasUntapLock) {
                    p.untap();
                }
                p.setSummoningSick(false);
                p.setLoyaltyActivationsThisTurn(0);
            });
        }

        String untapLog = activePlayerName + " untaps their permanents.";
        gameBroadcastService.logAndBroadcast(gameData, untapLog);
        log.info("Game {} - {} untaps their permanents", gameData.id, activePlayerName);

        // Queue may-not-untap choices for tapped permanents with MayNotUntapDuringUntapStepEffect
        for (Permanent p : mayNotUntapPermanents) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    p.getCard(),
                    activePlayerId,
                    List.of(new MayNotUntapDuringUntapStepEffect()),
                    "Untap " + p.getCard().getName() + "?"
            ));
        }

        // Untap permanents for non-active players that have "untap during each other player's step" effects
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return;

            List<UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect> untapEffects =
                    collectUntapOnEachOtherPlayersStepEffects(gameData, playerId, TurnStep.UNTAP);
            if (untapEffects.isEmpty()) return;

            boolean hasUnfilteredEffect = untapEffects.stream().anyMatch(e -> e.filter() == null);

            for (Permanent p : playerBattlefield) {
                if (hasUnfilteredEffect || untapEffects.stream().anyMatch(e -> e.filter() != null
                        && gameQueryService.matchesPermanentPredicate(gameData, p, e.filter()))) {
                    p.untap();
                }
            }

            String playerName = gameData.playerIdToName.get(playerId);
            if (hasUnfilteredEffect) {
                String seedbornLog = playerName + " untaps their permanents due to Seedborn Muse.";
                gameBroadcastService.logAndBroadcast(gameData, seedbornLog);
                log.info("Game {} - {} untaps permanents due to Seedborn Muse", gameData.id, playerName);
            } else {
                String filteredLog = playerName + " untaps some permanents during opponent's untap step.";
                gameBroadcastService.logAndBroadcast(gameData, filteredLog);
                log.info("Game {} - {} untaps filtered permanents during opponent's untap step", gameData.id, playerName);
            }
        });
    }

    List<UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect> collectUntapOnEachOtherPlayersStepEffects(
            GameData gameData, UUID playerId, TurnStep step) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        List<UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect> result = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect
                        && configuredEffect.step() == step) {
                    result.add(configuredEffect);
                }
            }
        }
        return result;
    }
}
