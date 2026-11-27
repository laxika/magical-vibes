package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Shared destruction/sacrifice helpers used by every "normal" Destruction effect handler and by
 * input handlers (forced sacrifice, pile separation, destroy-rest flows).
 *
 * <p>Extracted verbatim from {@code DestructionResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DestructionSupport {

    private final CreatureControlService creatureControlService;
    private final PermanentRemovalService permanentRemovalService;
    private final DepartureService departureService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService;
    private final ObjectProvider<DamageSupport> damageSupportProvider;
    private final ObjectProvider<PermanentControlSupport> permanentControlSupportProvider;

    public void beginNextDestroyRestChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                           List<UUID> protectedIds, String sourceName) {
        beginNextDestroyRestChoice(gameData, choosers, protectedIds, sourceName,
                new PermanentIsCreaturePredicate(), "Choose a creature to keep.", false);
    }

    public void beginNextDestroyRestChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                           List<UUID> protectedIds, String sourceName,
                                           PermanentPredicate destructionFilter, String choicePrompt,
                                           boolean requiresChoice) {
        beginNextDestroyRestChoice(gameData, choosers, protectedIds, sourceName,
                destructionFilter, choicePrompt, requiresChoice, requiresChoice ? 1 : 0);
    }

    public void beginNextDestroyRestChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                           List<UUID> protectedIds, String sourceName,
                                           PermanentPredicate destructionFilter, String choicePrompt,
                                           int requiredCount) {
        beginNextDestroyRestChoice(gameData, choosers, protectedIds, sourceName,
                destructionFilter, choicePrompt, requiredCount > 0, requiredCount);
    }

    private void beginNextDestroyRestChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                           List<UUID> protectedIds, String sourceName,
                                           PermanentPredicate destructionFilter, String choicePrompt,
                                           boolean requiresChoice, int requiredCount) {
        if (choosers.isEmpty()) return;
        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remainingChoosers = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.DestroyRestChoice(
                        remainingChoosers, List.copyOf(protectedIds), sourceName,
                        destructionFilter, choicePrompt, requiresChoice, requiredCount),
                choicePrompt + " The rest will be destroyed.");
    }

    public void completeDestroyRestChoice(GameData gameData, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.DestroyRestChoice context) {
        // Add the chosen creature to the protected set
        List<UUID> protectedIds = new ArrayList<>(context.protectedIds());
        protectedIds.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            // More players need to choose — prompt the next one
            beginNextDestroyRestChoice(gameData, context.remainingChoosers(), protectedIds, context.sourceName(),
                    context.destructionFilter(), context.choicePrompt(), context.requiredCount());
            return;
        }

        // All players have chosen — destroy all non-protected creatures
        String sourceName = context.sourceName();
        performDestroyAllMatchingExcept(gameData, sourceName != null ? sourceName : "unknown", protectedIds,
                context.destructionFilter());
    }

    public void performDestroyAllCreaturesExcept(GameData gameData, String sourceName, List<UUID> protectedIdList) {
        performDestroyAllMatchingExcept(gameData, sourceName, protectedIdList, new PermanentIsCreaturePredicate());
    }

    public void performDestroyAllMatchingExcept(GameData gameData, String sourceName,
                                                List<UUID> protectedIdList, PermanentPredicate filter) {
        Set<UUID> protectedIds = new HashSet<>(protectedIdList);

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, filter)
                        && !protectedIds.contains(perm.getId())) {
                    toDestroy.add(perm);
                }
            }
        });

        if (toDestroy.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but no creatures are destroyed."));
            return;
        }

        destroyBatch(gameData, toDestroy, sourceName, false);
    }

    public void destroyNonlandPermanentsByManaValue(GameData gameData, int targetManaValue,
                                                      String cardName, Set<UUID> playerFilter) {
        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerFilter != null && !playerFilter.contains(playerId)) return;
            for (Permanent perm : battlefield) {
                if (gameQueryService.isLand(gameData, perm)) {
                    continue;
                }
                // A face-down permanent has no mana cost, so its mana value is 0 whatever it was face up.
                int manaValue = perm.isFaceDown() ? 0 : perm.getCard().getManaValue();
                if (manaValue == targetManaValue) {
                    toDestroy.add(perm);
                }
            }
        });

        if (toDestroy.isEmpty()) {
            String logEntry = cardName + " resolves but finds no nonland permanents with mana value " + targetManaValue + ".";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} finds no nonland permanents with mana value {}", gameData.id, cardName, targetManaValue);
            return;
        }

        destroyBatch(gameData, toDestroy, cardName, false);
    }

    /** @return the number of permanents actually destroyed (indestructible / regenerated don't count) */
    public int destroyBatch(GameData gameData, List<Permanent> toDestroy, String sourceName,
                              boolean cannotBeRegenerated) {
        return destroyBatchCollecting(gameData, toDestroy, sourceName, cannotBeRegenerated).size();
    }

    /**
     * Same batch destruction as {@link #destroyBatch}, but returns the permanents that actually died
     * (were put into a graveyard), for "died this way" and "put into a graveyard this way" riders. A
     * "destroyed this way" rider counts {@link #destroyBatchDestroyed} instead.
     */
    public List<Permanent> destroyBatchCollecting(GameData gameData, List<Permanent> toDestroy, String sourceName,
                              boolean cannotBeRegenerated) {
        return destroyBatchWithOutcome(gameData, toDestroy, sourceName, cannotBeRegenerated).died();
    }

    /**
     * Same batch destruction as {@link #destroyBatch}, but returns every permanent that was destroyed,
     * whether it then went to a graveyard or a replacement such as Rest in Peace moved it elsewhere.
     * Indestructible, shield-countered and regenerated permanents are not included.
     */
    public List<Permanent> destroyBatchDestroyed(GameData gameData, List<Permanent> toDestroy, String sourceName,
                                                 boolean cannotBeRegenerated) {
        return destroyBatchWithOutcome(gameData, toDestroy, sourceName, cannotBeRegenerated).destroyed();
    }

    /**
     * Same batch destruction as {@link #destroyBatch}, but runs {@code beforeDestruction} once the batch's
     * death replacements have been asked (CR 616.1) and before the first permanent is destroyed. Lets an
     * effect whose resolution would otherwise change the game between choosing the batch and destroying it
     * (Voter's Dilemma finishing its vote) be stopped and resolved again for a death replacement choice;
     * {@code beforeDestruction} runs with that window already closed.
     */
    public int destroyBatch(GameData gameData, List<Permanent> toDestroy, String sourceName,
                            boolean cannotBeRegenerated, Runnable beforeDestruction) {
        return destroyBatchWithOutcome(gameData, toDestroy, sourceName, cannotBeRegenerated, beforeDestruction)
                .died().size();
    }

    /**
     * The permanents a batch destruction destroyed, those of them that then died (went to a graveyard rather than
     * somewhere a replacement such as Rest in Peace put them), and the departure result of every permanent it was
     * told to destroy, those that stayed included, with their last-known information (CR 608.2h).
     */
    public record BatchOutcome(List<Permanent> destroyed, List<Permanent> died, List<DepartureResult> results) {
    }

    /** Same batch destruction as {@link #destroyBatch}, returning both the destroyed and the died permanents. */
    public BatchOutcome destroyBatchWithOutcome(GameData gameData, List<Permanent> toDestroy, String sourceName,
                                                 boolean cannotBeRegenerated) {
        return destroyBatchWithOutcome(gameData, toDestroy, sourceName, cannotBeRegenerated, () -> { });
    }

    private BatchOutcome destroyBatchWithOutcome(GameData gameData, List<Permanent> toDestroy, String sourceName,
                                                 boolean cannotBeRegenerated, Runnable beforeDestruction) {
        Map<UUID, Permanent> byId = new java.util.LinkedHashMap<>();
        toDestroy.forEach(permanent -> byId.putIfAbsent(permanent.getId(), permanent));
        DepartureService.Outcome.Done done = departDestroyBatch(gameData, List.copyOf(byId.values()), sourceName,
                cannotBeRegenerated, beforeDestruction);
        List<Permanent> destroyed = new ArrayList<>();
        List<Permanent> died = new ArrayList<>();
        for (DepartureResult result : done.results()) {
            Permanent perm = byId.get(result.permanentId());
            if (!result.destroyed()) {
                continue;
            }
            destroyed.add(perm);
            if (diedFromBattlefield(result, perm)) {
                died.add(perm);
            }
        }
        return new BatchOutcome(destroyed, died, done.results());
    }

    /**
     * Destroys {@code toDestroy} as one event through {@link DepartureService}: every destruction replacement
     * (indestructible, regeneration, shield counters, ...) and CR 616.1 death replacement is settled while all of
     * them are still on the battlefield, then they are destroyed together. Inside a resolving effect that can be run
     * again ({@code AsksDeathReplacementsFirst}) a question stops the effect, which is resolved again with the
     * answers. {@code beforeDestruction} runs once every question is answered, before anything is destroyed.
     *
     * @return one result per distinct permanent, in the order given
     */
    public DepartureService.Outcome.Done departDestroyBatch(GameData gameData, List<Permanent> toDestroy,
                                                            String sourceName, boolean cannotBeRegenerated,
                                                            Runnable beforeDestruction) {
        return departDestroyBatch(gameData, toDestroy, sourceName, cannotBeRegenerated, beforeDestruction,
                (intent, perm, snapshot, removed) -> { });
    }

    private DepartureService.Outcome.Done departDestroyBatch(GameData gameData, List<Permanent> toDestroy,
                                                             String sourceName, boolean cannotBeRegenerated,
                                                             Runnable beforeDestruction,
                                                             DepartureService.AfterEach afterEach) {
        List<DepartureIntent> intents = toDestroy.stream()
                .map(Permanent::getId)
                .distinct()
                .map(permanentId -> DepartureIntent.destroy(permanentId, cannotBeRegenerated))
                .toList();
        return departureService.departNow(gameData, DepartureBatch.of(intents), new DepartureService.Hooks(
                () -> {
                    beforeDestruction.run();
                    for (Permanent perm : toDestroy.stream().distinct().toList()) {
                        if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                            gameLogService.append(gameData, GameLog.isIndestructible(perm.getCard()));
                        }
                    }
                },
                (intent, perm, snapshot, removed) -> {
                    afterEach.departed(intent, perm, snapshot, removed);
                    if (snapshot == null) {
                        // The destruction couldn't touch it (its controller's own effects can't affect it).
                        return;
                    }
                    gameLogService.append(gameData, GameLog.isDestroyed(perm.getCard()));
                    log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
                }));
    }

    /**
     * Per CR 700.4 a creature dies only when it is put into a graveyard from the battlefield, so a permanent
     * whose removal was replaced by exile or by a return to hand does not count. A token that ceased to exist
     * on its way counts unless it was exiled instead.
     */
    private static boolean diedFromBattlefield(DepartureResult result, Permanent perm) {
        return result.died(perm.getCard().isToken());
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        return tryDestroyAndLog(gameData, target, sourceName, cannotBeRegenerated, true);
    }

    /**
     * Destroys {@code target} as a one-permanent {@link DepartureBatch} through {@link #departDestroyBatch}: its
     * destruction replacements and CR 616.1 death replacements are settled before it leaves, asked inside a caller
     * that can be stopped and resolved again ({@code AsksDeathReplacementsFirst}). It can't end a resolution step,
     * since its callers go on in the same handler; a "destroy it, then …" handler that should pause departs through
     * {@link StepDepartureSupport} instead.
     *
     * @param cleanUpAttachments whether Auras left attached to nothing are put into the graveyard afterwards
     *                           ({@code false} lets the caller reattach an Aura first)
     * @return whether it was destroyed (indestructible, shielded or regenerated permanents were not)
     */
    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName,
                                    boolean cannotBeRegenerated, boolean cleanUpAttachments) {
        boolean[] destroyed = {false};
        departDestroyBatch(gameData, List.of(target), sourceName, cannotBeRegenerated, () -> { },
                (intent, permanent, snapshot, removed) -> destroyed[0] = snapshot != null);
        if (destroyed[0] && cleanUpAttachments) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        return destroyed[0];
    }

    /**
     * {@code playerId} sacrifices {@code permanent}, as a one-permanent {@link #sacrificeTogether}: its CR 616.1
     * death replacements are chosen before it leaves when its caller can be stopped and run again (a marked
     * handler's window, or a choice completion's replay window), and the sacrifice is logged and fires "whenever you
     * sacrifice" triggers only if it happened (CR 701.21a).
     *
     * @return what happened to it; read last-known power, toughness and card types from here
     */
    public DepartureResult sacrificeAndLog(GameData gameData, Permanent permanent, UUID playerId) {
        return sacrificeTogether(gameData, List.of(permanent), playerId).results().getFirst();
    }

    /**
     * Records {@code toSacrifice} as it is now on {@code entry}, the resolution it is about to be sacrificed for: its
     * power, toughness and colour count, and a snapshot with its card types and subtypes, which "the sacrificed
     * creature's power" and a reflexive "when you sacrifice [it] this way" read once it is gone (CR 608.2h). Changes
     * nothing on the battlefield.
     */
    public void recordSacrificedOn(GameData gameData, StackEntry entry, Permanent toSacrifice) {
        Permanent snapshot = new Permanent(toSacrifice);
        Set<com.github.laxika.magicalvibes.model.CardType> types =
                gameQueryService.getEffectiveCardTypes(gameData, toSacrifice);
        Card characteristics = toSacrifice.getCard().createRuntimeCopy();
        characteristics.setType(types.stream().findFirst().orElse(null));
        characteristics.setAdditionalTypes(Set.copyOf(types));
        characteristics.setSubtypes(
                permanentRemovalService.snapshotEffectivePermanentCard(gameData, toSacrifice).getSubtypes());
        characteristics.freeze();
        snapshot.setCard(characteristics);
        entry.setSacrificedPermanentSnapshot(snapshot);
        entry.setSacrificedPower(gameQueryService.getEffectivePower(gameData, toSacrifice));
        entry.setSacrificedColorCount(gameQueryService.getEffectiveColors(gameData, toSacrifice).size());
        entry.setSacrificedToughness(gameQueryService.getEffectiveToughness(gameData, toSacrifice));
    }

    private void logSacrifice(GameData gameData, Card sacrificedCard, UUID playerId, boolean sacrificedCreature) {
        gameData.playersWhoSacrificedPermanentsThisTurn.add(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.playerSacrifices(playerName, sacrificedCard));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, sacrificedCard.getName());
        // Collect both ally-permanent-sacrificed and global creature-sacrificed triggers for the
        // edict / chosen / forced-sacrifice paths that funnel through this shared helper.
        ActionEvents.sacrificed(gameData, playerId, sacrificedCard);
    }

    /**
     * A single player sacrifices {@code count} permanents matching {@code filter}: if they control
     * more than {@code count} they choose which (multi-select), otherwise all matching are sacrificed.
     * Reuses the same {@link MultiPermanentChoiceContext.ForcedSacrifice} direct-select flow as the
     * forced-sacrifice family. Callers must ensure at least {@code count} matching permanents exist.
     */
    public void sacrificePlayerMatchingPermanents(GameData gameData, UUID playerId, int count,
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }
        List<Permanent> matching = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter))
                .toList();
        if (matching.size() <= count) {
            performSimultaneousSacrifice(gameData, matching.stream().map(Permanent::getId).toList());
        } else {
            List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
            playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, count,
                    new MultiPermanentChoiceContext.ForcedSacrifice(playerId, List.of(), List.of()),
                    "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to sacrifice.");
        }
    }

    /**
     * A single player returns {@code count} permanents matching {@code filter} to their owners'
     * hands: if they control more than {@code count} they choose which (multi-select), otherwise
     * all matching are bounced. Callers must ensure at least {@code count} matching permanents exist.
     */
    public void returnPlayerMatchingPermanents(GameData gameData, UUID playerId, int count,
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }
        List<Permanent> matching = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter))
                .toList();
        if (matching.size() <= count) {
            List<Card> bouncedCards = new ArrayList<>();
            for (Permanent perm : matching) {
                if (permanentRemovalService.removePermanentToHand(gameData, perm)) {
                    bouncedCards.add(perm.getCard());
                }
            }
            if (!bouncedCards.isEmpty()) {
                permanentRemovalService.removeOrphanedAuras(gameData);
                gameLogService.append(gameData,
                        GameLog.text(gameData.playerIdToName.get(playerId) + " returns "
                                + bouncedCards.stream().map(Card::getName).collect(java.util.stream.Collectors.joining(", "))
                                + (bouncedCards.size() == 1 ? " to its owner's hand." : " to their owners' hands.")));
            }
        } else {
            List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
            playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, count,
                    new MultiPermanentChoiceContext.ForcedReturnToHand(playerId),
                    "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to return to hand.");
        }
    }

    /**
     * A single player destroys {@code count} permanents matching {@code filter} that they control:
     * if they control more than {@code count} they choose which (multi-select via
     * {@link MultiPermanentChoiceContext.ForcedDestroy}), otherwise all matching are destroyed with
     * no choice. Destruction respects regeneration/indestructible. Returns {@code true} if a choice
     * was begun (resolution is now awaiting input), {@code false} if it resolved synchronously.
     */
    public boolean destroyPlayerMatchingPermanents(GameData gameData, UUID playerId, int count,
            com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter, String sourceName) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> matching = battlefield == null ? List.of() : battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter))
                .filter(p -> !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE))
                .toList();

        if (matching.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no matching permanents to destroy."));
            return false;
        }

        if (matching.size() <= count) {
            destroyBatch(gameData, matching, sourceName, false);
            permanentRemovalService.removeOrphanedAuras(gameData);
            return false;
        }

        List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
        playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, count,
                new MultiPermanentChoiceContext.ForcedDestroy(playerId, sourceName),
                "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to destroy.");
        return true;
    }

    /** Deals {@code damage} from {@code entry}'s source to a player through the damage pipeline. */
    public void dealNoncombatDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int damage) {
        damageSupportProvider.getObject().dealDamageToPlayer(gameData, entry, playerId, damage);
    }

    public List<UUID> collectCreatureIds(GameData gameData, UUID playerId, Predicate<Permanent> additionalFilter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<UUID> ids = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p) && additionalFilter.test(p)) {
                    ids.add(p.getId());
                }
            }
        }
        return ids;
    }

    public List<UUID> collectPermanentIds(GameData gameData, UUID playerId, Predicate<Permanent> filter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<UUID> ids = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (filter.test(p)) {
                    ids.add(p.getId());
                }
            }
        }
        return ids;
    }

    /** Collects matching permanents on other players' battlefields for a gain-control cost. */
    public List<UUID> collectPermanentIdsNotControlledBy(GameData gameData, UUID playerId,
            PermanentPredicate filter) {
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(playerId);
        List<UUID> ids = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!controllerId.equals(playerId)
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context)) {
                ids.add(permanent.getId());
            }
        });
        return ids;
    }

    /**
     * Permanents the player controls that carry at least one counter of any kind — the legal
     * choices for {@link com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost}.
     */
    public List<UUID> collectPermanentIdsWithAnyCounter(GameData gameData, UUID playerId) {
        return collectPermanentIds(gameData, playerId,
                p -> p.getCounters().values().stream().anyMatch(count -> count > 0));
    }

    /**
     * Removes one counter from {@code permanent}, asking the player to choose its kind when
     * several kinds are present. Returns false when the permanent has no counters left to remove.
     */
    public boolean removeOneCounterAndLog(GameData gameData, Permanent permanent, UUID playerId) {
        List<CounterType> kinds = permanent.getCounters().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(java.util.Map.Entry::getKey)
                .toList();
        if (kinds.isEmpty()) {
            return false;
        }
        if (kinds.size() > 1) {
            playerInputService.beginRemoveOneCounterChoice(gameData, playerId, permanent.getId(),
                    FaceDownVisibility.publicName(gameData, permanent), kinds);
            return true;
        }
        CounterType kind = kinds.getFirst();
        permanent.setCounterCount(kind, permanent.getCounterCount(kind) - 1);
        if (kind == CounterType.OIL) {
            gameData.recordOilCounterRemoved(permanent, 1);
        }
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " removes a counter from ", permanent.getCard(), "."));
        log.info("Game {} - {} removes a {} counter from {}", gameData.id, playerName, kind,
                permanent.getCard().getName());
        return true;
    }

    /**
     * CR 616.1 for permanents an effect sacrifices one after another as part of the same event:
     * asks every death replacement choice among them before the first is sacrificed, while the
     * resolving effect can still be stopped and resolved again.
     */
    public void askDeathReplacementsBeforeSacrifice(GameData gameData, List<Permanent> permanents) {
        permanentRemovalService.askDeathReplacementsBeforeRemoval(gameData, permanents.stream()
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList());
    }

    public void performSimultaneousSacrifice(GameData gameData, List<UUID> ids) {
        List<Permanent> permanents = ids.stream()
                .distinct()
                .map(permId -> gameQueryService.findPermanentById(gameData, permId))
                .filter(java.util.Objects::nonNull)
                .filter(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent))
                .toList();
        sacrificeTogether(gameData, permanents, null);
    }

    /**
     * Each controller sacrifices their permanents among {@code permanents} at the same time, as one event
     * through {@link DepartureService}: every CR 616.1 death replacement is chosen in APNAP order while all of
     * them are still on the battlefield. Inside a caller that can be run again a question stops it, and it is run
     * again with the answers. Each sacrifice is logged and its sacrifice triggers collected as it happens.
     *
     * @param sacrificingPlayerId the player credited with every sacrifice, or {@code null} for each controller
     * @return one result per distinct permanent, in the order given
     */
    public DepartureService.Outcome.Done sacrificeTogether(GameData gameData, List<Permanent> permanents,
                                                           UUID sacrificingPlayerId) {
        List<DepartureIntent> intents = permanents.stream()
                .map(Permanent::getId)
                .distinct()
                .map(DepartureIntent::sacrifice)
                .toList();
        // Sacrificed at the same time, so "another creature leaves" watchers that are also sacrificed still see the others leave.
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        gameData.simultaneousSacrificeDepth++;
        try {
            DepartureService.Outcome.Done done = departureService.departNow(gameData, DepartureBatch.of(intents),
                    DepartureService.Hooks.afterEach((intent, perm, snapshot, removed) -> {
                        if (snapshot == null) {
                            // It couldn't be sacrificed (CR 701.21a), so it wasn't, and nothing that watches for a
                            // sacrifice sees one.
                            return;
                        }
                        UUID playerId = sacrificingPlayerId != null ? sacrificingPlayerId : snapshot.controllerId();
                        logSacrifice(gameData, perm.getCard(), playerId, snapshot.creature());
                    }));
            recordOnSacrificeStep(gameData, done.results());
            return done;
        } finally {
            if (--gameData.simultaneousSacrificeDepth == 0) {
                gameData.sacrificeBoonBatchPlayerIds.clear();
            }
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
    }

    /**
     * Adds what was sacrificed to the sacrifice step of the resolution it happens in, the resolving entry or the one
     * parked on that step's prompt, once such a step has begun ({@link StackEntry#beginSacrificeStep}): "the sacrificed
     * creature's power" and "if you do" then read its last-known information there (CR 608.2h).
     */
    private static void recordOnSacrificeStep(GameData gameData, List<DepartureResult> results) {
        StackEntry stepEntry = gameData.resolvingEntry != null
                ? gameData.resolvingEntry : gameData.pendingEffectResolutionEntry;
        if (stepEntry != null && stepEntry.getResolutionResults().outcomes().containsKey(StepKind.SACRIFICE)) {
            stepEntry.addSacrificeDepartures(results);
        }
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds) {
        beginNextForcedSacrificeFromQueue(gameData, choosers, accumulatedSacrificeIds, false, null);
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds, boolean simultaneousFlow) {
        beginNextForcedSacrificeFromQueue(
                gameData, choosers, accumulatedSacrificeIds, simultaneousFlow, null);
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds, boolean simultaneousFlow,
                                                  com.github.laxika.magicalvibes.model.LibrarySearchFollowUp afterSacrifices) {
        beginNextForcedSacrificeFromQueue(gameData, choosers, accumulatedSacrificeIds, simultaneousFlow,
                afterSacrifices, false);
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds, boolean simultaneousFlow,
                                                  com.github.laxika.magicalvibes.model.LibrarySearchFollowUp afterSacrifices,
                                                  boolean recordSacrificedCount) {
        beginNextForcedSacrificeFromQueue(gameData, choosers, accumulatedSacrificeIds, simultaneousFlow,
                afterSacrifices, recordSacrificedCount, false);
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds, boolean simultaneousFlow,
                                                  com.github.laxika.magicalvibes.model.LibrarySearchFollowUp afterSacrifices,
                                                  boolean recordSacrificedCount, boolean recordSacrificedPower) {
        if (choosers.isEmpty()) {
            return;
        }

        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remainingChoosers = List.copyOf(choosers.subList(1, choosers.size()));
        // A permanent already chosen for an earlier prompt of the same sacrifice cannot be chosen again.
        List<UUID> stillValid = next.validPermanentIds().stream()
                .filter(id -> !accumulatedSacrificeIds.contains(id)).toList();
        if (!stillValid.isEmpty() && stillValid.size() != next.validPermanentIds().size()) {
            next = new PendingForcedSacrifice(next.playerId(), Math.min(next.count(), stillValid.size()), stillValid);
        }
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.ForcedSacrifice(next.playerId(), remainingChoosers,
                        List.copyOf(accumulatedSacrificeIds), simultaneousFlow, recordSacrificedCount,
                        recordSacrificedPower, afterSacrifices),
                "Choose " + next.count() + " permanent"
                        + (next.count() > 1 ? "s" : "") + " to sacrifice.");
    }

    public void performSacrificeCreatureForPlayer(GameData gameData, UUID targetPlayerId) {
        List<UUID> creatureIds = collectCreatureIds(gameData, targetPlayerId,
                p -> !gameQueryService.cantBeChosenToSacrifice(gameData, p));

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to sacrifice.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no creatures to sacrifice", gameData.id, playerName);
            return;
        }

        if (creatureIds.size() == 1) {
            // Only one creature — sacrifice it automatically
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                sacrificeAndLog(gameData, creature, targetPlayerId);
            }
            return;
        }

        // Multiple creatures — prompt player to choose
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(targetPlayerId));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature to sacrifice.");
    }

    public void gainPermanentControl(GameData gameData, Permanent target, UUID controllerId, String sourceCardName) {
        creatureControlService.applyControlEffect(gameData, controllerId, target,
                new GainControlOfTargetEffect(Duration.PERMANENT), Duration.PERMANENT, null,
                sourceCardName);
    }

    public void createTokenForPlayer(GameData gameData, UUID controllerId,
                                      CreateTokenEffect token, String sourceName, String sourceSetCode) {
        createTokenForPlayer(gameData, controllerId, token, 1, sourceName, sourceSetCode);
    }

    /**
     * Creates {@code tokenCount} tokens from {@code token} for {@code controllerId} as one token creation event,
     * through {@link PermanentControlSupport#applyCreateToken}, so the creating player orders that event's
     * count replacements and additions (CR 616.1); the creation may pause for that choice.
     */
    public void createTokenForPlayer(GameData gameData, UUID controllerId,
                                       CreateTokenEffect token, int tokenCount,
                                       String sourceName, String sourceSetCode) {
        if (token.investigate()) {
            for (int action = 0; action < tokenCount; action++) {
                ActionEvents.investigated(gameData, controllerId);
            }
        }
        List<UUID> createdIds = permanentControlSupportProvider.getObject()
                .applyCreateToken(gameData, controllerId, token, tokenCount, sourceSetCode);
        log.info("Game {} - {} creates {} token(s) for {}", gameData.id, sourceName, createdIds.size(),
                gameData.playerIdToName.get(controllerId));
    }
}
