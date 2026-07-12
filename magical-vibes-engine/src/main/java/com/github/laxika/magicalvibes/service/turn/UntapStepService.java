package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.StorageMatrixEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    /**
     * Performs the untap step for the active player.
     *
     * <ol>
     *   <li>Removes stale untap-prevention locks whose source permanent has left
     *       the battlefield or is no longer tapped.</li>
     *   <li>Untaps each of the active player's permanents unless it has a self-scoped
     *       {@link DoesntUntapEffect}, an attached (enchanted-scope) {@link DoesntUntapEffect},
     *       or an active untap lock.</li>
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
        untapPermanents(gameData, activePlayerId, null);
    }

    /**
     * Performs the untap step, optionally restricting which of the active player's permanents may
     * untap to those matching {@code restrictPredicate} (Storage Matrix). A {@code null} predicate
     * means no restriction (the normal untap step). The restriction applies only to the active
     * player's own permanents, not to "untap during each other player's step" effects (Seedborn
     * Muse), which untap during a different player's untap step and so are unaffected.
     *
     * @param restrictPredicate only permanents matching this untap; {@code null} = untap all
     */
    public void untapPermanents(GameData gameData, UUID activePlayerId, PermanentPredicate restrictPredicate) {
        untapPermanents(gameData, activePlayerId, restrictPredicate, false);
    }

    /**
     * Performs the untap step, optionally skipping the untapping entirely (Savor the Moment's "skip the
     * untap step of that turn"). When {@code skipUntapStep} is true, none of the active player's
     * permanents untap and no Seedborn-Muse-style cross-player untap or may-not-untap choices occur,
     * but summoning sickness and loyalty-activation flags are still cleared so the player can still
     * attack with and use creatures they already controlled.
     *
     * @param skipUntapStep {@code true} to keep every permanent tapped through this untap step
     */
    public void untapPermanents(GameData gameData, UUID activePlayerId, PermanentPredicate restrictPredicate,
                                boolean skipUntapStep) {
        String activePlayerName = gameData.playerIdToName.get(activePlayerId);

        if (skipUntapStep) {
            List<Permanent> ownBattlefield = gameData.playerBattlefields.get(activePlayerId);
            if (ownBattlefield != null) {
                ownBattlefield.forEach(p -> {
                    // Permanents stay tapped, but a queued "skip next untap" is still consumed (this
                    // untap step would have been its chance to untap) and summoning sickness clears.
                    if (p.getSkipUntapCount() > 0) {
                        p.setSkipUntapCount(p.getSkipUntapCount() - 1);
                    }
                    p.setSummoningSick(false);
                    p.setLoyaltyActivationsThisTurn(0);
                });
            }
            String skipLog = activePlayerName + " skips their untap step.";
            gameBroadcastService.logAndBroadcast(gameData, skipLog);
            log.info("Game {} - {} skips their untap step", gameData.id, activePlayerName);
            return;
        }

        // Clean up stale untap-prevention locks on ALL battlefields before untapping.
        // A lock is stale if the source permanent is no longer on the battlefield or is no longer tapped.
        gameData.forEachPermanent((pid, p) -> {
            if (!p.getUntapPreventedByPermanentIds().isEmpty()) {
                p.getUntapPreventedByPermanentIds().removeIf(sourceId -> {
                    Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
                    return source == null || !source.isTapped();
                });
            }
            // Clean up "while source on battlefield" locks — only removed when source leaves battlefield.
            if (!p.getUntapPreventedWhileSourceOnBattlefieldIds().isEmpty()) {
                p.getUntapPreventedWhileSourceOnBattlefieldIds().removeIf(sourceId -> {
                    Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
                    return source == null;
                });
            }
        });

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> mayNotUntapPermanents = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                // ENCHANTED-scope DoesntUntapEffect on an attached aura keeps the host tapped.
                boolean hasAttachedDoesntUntap = gameQueryService.hasAuraWithEffect(gameData, p, DoesntUntapEffect.class);
                boolean hasSelfDoesntUntap = p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof DoesntUntapEffect d && d.scope() == TapUntapScope.SELF);
                boolean hasMayNotUntap = p.isTapped() && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
                boolean hasUntapLock = !p.getUntapPreventedByPermanentIds().isEmpty()
                        || !p.getUntapPreventedWhileSourceOnBattlefieldIds().isEmpty();
                boolean skipsNextUntap = p.getSkipUntapCount() > 0;
                // A global static (e.g. Marble Titan) can lock this permanent based on a predicate.
                boolean hasMatchingDoesntUntap = matchingStaticPreventsUntap(gameData, p);

                boolean blockedByStorageMatrix = restrictPredicate != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, restrictPredicate);

                if (skipsNextUntap) {
                    // Decrement skip counter but don't untap this step (e.g. Vorinclex)
                    p.setSkipUntapCount(p.getSkipUntapCount() - 1);
                } else if (blockedByStorageMatrix) {
                    // Storage Matrix: not the chosen permanent type — stays tapped this step
                } else if (hasMayNotUntap) {
                    // Present choice to controller later — skip untap for now
                    mayNotUntapPermanents.add(p);
                } else if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasUntapLock
                        && !hasMatchingDoesntUntap) {
                    tapUntapSupport.untapPermanent(gameData, p);
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
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()))) {
                    tapUntapSupport.untapPermanent(gameData, p);
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

    /**
     * Returns {@code true} if a Storage Matrix untap restriction is in force for the given active
     * player: some untapped permanent (any controller) carries a {@link StorageMatrixEffect} and
     * the active player has at least one tapped permanent to decide about. When there is nothing
     * tapped, the choice would have no observable effect and is skipped.
     */
    public boolean storageMatrixRestrictionApplies(GameData gameData, UUID activePlayerId) {
        boolean untappedMatrixPresent = gameData.anyPermanentMatches(p -> !p.isTapped()
                && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof StorageMatrixEffect));
        if (!untappedMatrixPresent) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        return battlefield != null && battlefield.stream().anyMatch(Permanent::isTapped);
    }

    /**
     * Returns {@code true} if any permanent on any battlefield carries a
     * {@link MatchingPermanentsDoesntUntapEffect} whose filter matches the given permanent
     * (e.g. Marble Titan locking every creature with power 3 or greater, including its own).
     */
    private boolean matchingStaticPreventsUntap(GameData gameData, Permanent permanent) {
        return gameData.anyPermanentMatches(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof MatchingPermanentsDoesntUntapEffect lock
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, lock.filter())));
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
