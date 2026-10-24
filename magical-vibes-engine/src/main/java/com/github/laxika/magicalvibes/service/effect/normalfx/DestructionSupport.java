package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterRemovalSubject;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerThenTapSourceIfDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourceAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BounceScope;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedBlockOrBecomesBlockedTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayGainControlOfCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
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

    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final DepartureService departureService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService;
    private final LifeSupport lifeSupport;
    private final OpponentMayGainControlOfCreatureYouControlEffectHandler opponentMayGainControlHandler;
    private final OpponentGainsControlOfSourceCreatureEffectHandler opponentGainsControlOfSourceHandler;
    private final RemoveAllCountersEffectHandler removeAllCountersHandler;
    private final PhasingService phasingService;
    private final ExileSelfEffectHandler exileSelfEffectHandler;
    private final ExileSourceCardFromGraveyardEffectHandler exileSourceCardFromGraveyardEffectHandler;
    private final LibraryExileSupport libraryExileSupport;
    private final SacrificeEnchantedCreatureEffectHandler sacrificeEnchantedHandler;
    private final DealDamageToTargetAndTheirCreaturesEffectHandler damageTargetAndTheirCreaturesHandler;
    private final MakeCreatureUnblockableEffectHandler makeCreatureUnblockableHandler;
    private final BouncePermanentOnUpkeepEffectHandler bouncePermanentOnUpkeepEffectHandler;
    private final ReturnToHandEffectHandler returnToHandEffectHandler;
    private final ControllerLosesGameEffectHandler controllerLosesGameHandler;
    private final RegisterDelayedBlockOrBecomesBlockedTriggerEffectHandler registerDelayedBlockOrBecomesBlockedTriggerHandler;
    private final PreventDamageFromChosenSourceEffectHandler preventDamageFromChosenSourceHandler;
    private final BoostAllOwnCreaturesEffectHandler boostAllOwnCreaturesHandler;
    private final BounceSupport bounceSupport;
    private final EnergyCountersEffectHandler energyCountersEffectHandler;
    private final DrawCardEffectHandler drawCardEffectHandler;
    private final ObjectProvider<DamageSupport> damageSupportProvider;
    private final ObjectProvider<TapUntapSupport> tapUntapSupportProvider;

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
     * so a "for each permanent destroyed this way" rider can inspect them (e.g. their controllers).
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

    private record BatchOutcome(List<Permanent> destroyed, List<Permanent> died) {
    }

    private BatchOutcome destroyBatchWithOutcome(GameData gameData, List<Permanent> toDestroy, String sourceName,
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
        return new BatchOutcome(destroyed, died);
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
        return result.wentToGraveyard() || result.destination() == null && perm.getCard().isToken();
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        return tryDestroyAndLog(gameData, target, sourceName, cannotBeRegenerated, true);
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName,
                                    boolean cannotBeRegenerated, boolean cleanUpAttachments) {
        boolean destroyed = cleanUpAttachments
                ? permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated)
                : permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated, false);
        if (!destroyed) {
            return false;
        }
        gameLogService.append(gameData, GameLog.isDestroyed(target.getCard()));
        log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), sourceName);
        return true;
    }

    public void sacrificeAndLog(GameData gameData, Permanent creature, UUID playerId) {
        Card sacrificedCard = creature.getCard();
        boolean sacrificedCreature = gameQueryService.isCreature(gameData, creature);
        permanentRemovalService.sacrificePermanentToGraveyard(gameData, creature);
        logSacrifice(gameData, sacrificedCard, playerId, sacrificedCreature);
    }

    private void logSacrifice(GameData gameData, Card sacrificedCard, UUID playerId, boolean sacrificedCreature) {
        gameData.playersWhoSacrificedPermanentsThisTurn.add(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.playerSacrifices(playerName, sacrificedCard));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, sacrificedCard.getName());
        // Collect both ally-permanent-sacrificed and global creature-sacrificed triggers for the
        // edict / chosen / forced-sacrifice paths that funnel through this shared helper.
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, sacrificedCard, null,
                sacrificedCreature);
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
                    permanent.getCard().getName(), kinds);
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
            return departureService.departNow(gameData, DepartureBatch.of(intents), DepartureService.Hooks.afterEach(
                    (intent, perm, snapshot, removed) -> {
                        if (snapshot == null) {
                            // It couldn't be sacrificed (CR 701.21a), so it wasn't, and nothing that watches for a
                            // sacrifice sees one.
                            return;
                        }
                        UUID playerId = sacrificingPlayerId != null ? sacrificingPlayerId : snapshot.controllerId();
                        logSacrifice(gameData, perm.getCard(), playerId, snapshot.creature());
                    }));
        } finally {
            if (--gameData.simultaneousSacrificeDepth == 0) {
                gameData.sacrificeBoonBatchPlayerIds.clear();
            }
            permanentRemovalService.endPermanentLeaveBatch(gameData);
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

    public void completeForcedCostOrElse(GameData gameData, UUID permanentId,
                                         PermanentChoiceContext.ForcedCostOrElse context) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null) {
            // The fallback effects may change the game before they kill, so they can't stop and be redone.
            gameData.deathReplacementsCanRerunEffect = false;
            StackEntry syntheticEntry = new StackEntry(
                    com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                    context.sourceCard(),
                    context.controllerId(),
                    context.sourceCard().getName() + "'s ability",
                    List.of(context.effect()),
                    null,
                    context.sourcePermanentId());
            resolveForcedCostElseEffects(gameData, syntheticEntry, context.effect());
            gameData.forcedCostOrElseSourceControllerId = null;
            gameData.forcedCostOrElseRemainingPlayers.clear();
            return;
        }

        if (context.effect().forcedCost() instanceof GainControlOfPermanentsCost gainCost) {
            UUID payerId = context.controllerId();
            UUID currentControllerId = gameData.findControllerOf(target);
            FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(payerId);
            if (currentControllerId == null || currentControllerId.equals(payerId)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    target, gainCost.filter(), filterContext)) {
                resolveForcedCostElseEffectsFromContext(gameData, context);
                clearForcedCostOrElseState(gameData);
                return;
            }

            gainPermanentControl(gameData, target, payerId, context.sourceCard().getName());
            int remaining = gainCost.count() - 1;
            if (remaining <= 0) {
                clearForcedCostOrElseState(gameData);
                return;
            }

            List<UUID> remainingIds = collectPermanentIdsNotControlledBy(gameData, payerId, gainCost.filter());
            if (remainingIds.size() < remaining) {
                resolveForcedCostElseEffectsFromContext(gameData, context);
                clearForcedCostOrElseState(gameData);
                return;
            }

            ForcedCostOrElseEffect remainingEffect = new ForcedCostOrElseEffect(
                    new GainControlOfPermanentsCost(remaining, gainCost.filter()), context.effect().elseEffects(),
                    false, false, false, false, context.effect().paidEffects());
            if (remainingIds.size() == 1) {
                Permanent remainingTarget = gameQueryService.findPermanentById(gameData, remainingIds.getFirst());
                if (remainingTarget != null) {
                    gainPermanentControl(gameData, remainingTarget, payerId, context.sourceCard().getName());
                }
                clearForcedCostOrElseState(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ForcedCostOrElse(
                    payerId, context.sourcePermanentId(), context.sourceCard(), remainingEffect));
            playerInputService.beginPermanentChoice(gameData, payerId, remainingIds,
                    "Choose a permanent to gain control of.");
            clearForcedCostOrElseState(gameData);
            return;
        }

        if (context.effect().forcedCost() instanceof RemoveCounterFromControlledPermanentCost) {
            // "unless you remove a counter from a permanent you control" — the chosen permanent
            // sheds a counter instead of being sacrificed.
            removeOneCounterAndLog(gameData, target, context.controllerId());
        } else {
            sacrificeAndLog(gameData, target, context.controllerId());
        }
        gameData.forcedCostOrElseSourceControllerId = null;
        gameData.forcedCostOrElseRemainingPlayers.clear();
    }

    public void gainPermanentControl(GameData gameData, Permanent target, UUID controllerId, String sourceCardName) {
        creatureControlService.applyControlEffect(gameData, controllerId, target,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT, null,
                sourceCardName);
    }

    private void resolveForcedCostElseEffectsFromContext(GameData gameData,
            PermanentChoiceContext.ForcedCostOrElse context) {
        StackEntry syntheticEntry = new StackEntry(
                com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                context.sourceCard(), context.controllerId(), context.sourceCard().getName() + "'s ability",
                List.of(context.effect()), null, context.sourcePermanentId());
        resolveForcedCostElseEffects(gameData, syntheticEntry, context.effect());
    }

    private void clearForcedCostOrElseState(GameData gameData) {
        gameData.forcedCostOrElseSourceControllerId = null;
        gameData.forcedCostOrElseRemainingPlayers.clear();
    }

    /**
     * Resolves the unpaid branch ({@link ForcedCostOrElseEffect#elseEffects()}) in order. Only the first
     * fallback effect runs inside a death replacement rerun window its caller opened; each later one runs with
     * it closed, since a rerun would repeat what came before.
     */
    public void resolveForcedCostElseEffects(GameData gameData, StackEntry entry, ForcedCostOrElseEffect effect) {
        for (var elseEffect : effect.elseEffects()) {
            if (elseEffect instanceof TapPermanentsEffect tap && tap.scope() == TapUntapScope.SELF) {
                tapSourcePermanent(gameData, entry);
            } else if (elseEffect instanceof TapPermanentsEffect tap && tap.scope() == TapUntapScope.ENCHANTED) {
                // Mind Whip: "you tap that creature" — tap the permanent the source Aura enchants.
                tapEnchantedPermanent(gameData, entry);
            } else if (elseEffect instanceof DealDamageToPlayersEffect damage
                    && damage.recipient() == DamageRecipient.CONTROLLER
                    && damage.amount() instanceof Fixed fixed) {
                dealNoncombatDamageToPlayer(gameData, entry, entry.getControllerId(), fixed.value());
                gameOutcomeService.checkWinCondition(gameData);
            } else if (elseEffect instanceof DealDamageToPlayersEffect damage
                    && damage.recipient() == DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER
                    && damage.amount() instanceof Fixed fixed) {
                // Mind Whip: damage the enchanted permanent's controller (baked as targetId).
                UUID victim = entry.getTargetId();
                if (victim != null) {
                    dealNoncombatDamageToPlayer(gameData, entry, victim, fixed.value());
                    gameOutcomeService.checkWinCondition(gameData);
                }
            } else if (elseEffect instanceof SacrificeSelfEffect) {
                sacrificeSource(gameData, entry);
            } else if (elseEffect instanceof DrawCardEffect draw) {
                drawCardEffectHandler.resolve(gameData, entry, draw);
            } else if (elseEffect instanceof ReturnToHandEffect returnToHand
                    && returnToHand.scope() == BounceScope.SELF) {
                bounceSupport.applyReturnSelfToHand(gameData, entry);
            } else if (elseEffect instanceof EnergyCountersEffect energyCounters) {
                energyCountersEffectHandler.resolve(gameData, entry, energyCounters);
            } else if (elseEffect instanceof SacrificeEnchantedCreatureEffect sacrificeEnchanted) {
                // "that player sacrifices it unless they pay {X}" (Soul Tithe) — the enchanted
                // permanent, not the Aura, is sacrificed by its own controller.
                sacrificeEnchantedHandler.resolve(gameData, entry, sacrificeEnchanted);
            } else if (elseEffect instanceof com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect unblockable) {
                // "it gains 'this creature can't be blocked' until end of turn unless defending
                // player sacrifices a creature" (Ogre Marauder) — the attacking source.
                makeCreatureUnblockableHandler.resolve(gameData, entry, unblockable);
                Permanent grantedTo = entry.getSourcePermanentId() == null ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (grantedTo != null && grantedTo.isCantBeBlocked()) {
                    grantedTo.setCantBeBlockedGrantedAbility(true);
                }
            } else if (elseEffect instanceof ExileSelfEffect exileSelf) {
                // "exile this creature unless you sacrifice another creature" (Demonlord of Ashmouth).
                exileSelfEffectHandler.resolve(gameData, entry, exileSelf);
            } else if (elseEffect instanceof ExileSourceCardFromGraveyardEffect exileSource) {
                exileSourceCardFromGraveyardEffectHandler.resolve(gameData, entry, exileSource);
            } else if (elseEffect instanceof PhaseOutEffect phaseOut
                    && phaseOut.subject() == PhaseOutSubject.SOURCE) {
                // "unless you pay {cost}, this creature phases out" (Vaporous Djinn).
                phaseOutSource(gameData, entry);
            } else if (elseEffect instanceof LoseLifeEffect loseLife
                    && loseLife.recipient() == LoseLifeRecipient.CONTROLLER
                    && loseLife.amount() instanceof Fixed lifeAmount) {
                // "unless you pay {cost}, you lose N life" (Nafs Asp). Life loss, not damage
                // (CR 118.2) — never routed through damage plumbing.
                lifeSupport.applyLifeLoss(gameData, entry.getControllerId(), lifeAmount.value(), entry.getCard().getName());
                gameOutcomeService.checkWinCondition(gameData);
            } else if (elseEffect instanceof LoseLifeEffect loseLife
                    && loseLife.recipient() == LoseLifeRecipient.TARGET_PLAYER
                    && loseLife.amount() instanceof Fixed lifeAmount) {
                // "that player loses N life" (Pillar Tombs of Aku) — targetId is the acting player.
                UUID victim = entry.getTargetId();
                if (victim != null) {
                    lifeSupport.applyLifeLoss(gameData, victim, lifeAmount.value(), entry.getCard().getName());
                    gameOutcomeService.checkWinCondition(gameData);
                }
            } else if (elseEffect instanceof GivePoisonCountersEffect poison
                    && poison.recipient() == PoisonRecipient.CONTROLLER
                    && poison.amount() instanceof Fixed poisonAmount) {
                // "unless they pay {2}, they get another poison counter" (Sabertooth Cobra) — the
                // entry controller is the player who owes the payment.
                lifeSupport.applyPoisonCounters(gameData, entry.getControllerId(), poisonAmount.value(),
                        entry.getCard().getName(), entry.getControllerId());
                gameOutcomeService.checkWinCondition(gameData);
            } else if (elseEffect instanceof ControllerLosesGameEffect) {
                controllerLosesGameHandler.resolve(gameData, entry, elseEffect);
            } else if (elseEffect instanceof DestroySourceAndDamageControllerIfDestroyedEffect destroyDamage) {
                destroySourceAndDamageControllerIfDestroyed(gameData, entry, destroyDamage.damage());
            } else if (elseEffect instanceof DealDamageToControllerThenTapSourceIfDamageDealtEffect damageThenTap) {
                dealDamageToControllerThenTapSourceIfDealt(gameData, entry, damageThenTap.damage());
            } else if (elseEffect instanceof DestroyReferencedPermanentEffect destroySource
                    && destroySource.reference() == PermanentReference.SOURCE) {
                destroySource(gameData, entry, destroySource.cannotBeRegenerated());
            } else if (elseEffect instanceof com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect damageAndCreatures) {
                // "When a player doesn't pay this enchantment's cumulative upkeep, this enchantment
                // deals X damage to target player or planeswalker and each creature that player
                // controls" (Heart of Bogardan). The companion trigger has no target on this path,
                // so the source controller's opponent takes it.
                UUID victim = gameQueryService.getOpponentId(gameData, entry.getControllerId());
                if (victim != null) {
                    UUID previousTarget = entry.getTargetId();
                    entry.setTargetId(victim);
                    damageTargetAndTheirCreaturesHandler.resolve(gameData, entry, damageAndCreatures);
                    entry.setTargetId(previousTarget);
                }
            } else if (elseEffect instanceof com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect exileLibrary) {
                // "When a player doesn't pay this enchantment's cumulative upkeep, that player
                // exiles all cards from their library" (Thought Lash).
                libraryExileSupport.exileEntireLibrary(gameData, entry.getControllerId(), exileLibrary.faceDown());
            } else if (elseEffect instanceof RemoveAllCountersEffect removeCounters
                    && removeCounters.subject() == CounterRemovalSubject.SOURCE) {
                // "remove all wage counters from this creature" (Rogue Skycaptain).
                removeAllCountersHandler.resolve(gameData, entry, removeCounters);
            } else if (elseEffect instanceof RegisterDelayedBlockOrBecomesBlockedTriggerEffect delayed) {
                registerDelayedBlockOrBecomesBlockedTriggerHandler.resolve(gameData, entry, delayed);
            } else if (elseEffect instanceof OpponentGainsControlOfSourceCreatureEffect gainControl) {
                // "an opponent gains control of it" (Rogue Skycaptain).
                opponentGainsControlOfSourceHandler.resolve(gameData, entry, gainControl);
            } else if (elseEffect instanceof OpponentMayGainControlOfCreatureYouControlEffect steal) {
                opponentMayGainControlHandler.offer(gameData, entry, steal);
                // Interaction started — further else-effects would race the may-prompt.
                return;
            } else if (elseEffect instanceof BouncePermanentOnUpkeepEffect bounce) {
                bouncePermanentOnUpkeepEffectHandler.resolve(gameData, entry, bounce);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            } else if (elseEffect instanceof PreventDamageFromChosenSourceEffect preventDamage) {
                preventDamageFromChosenSourceHandler.resolve(gameData, entry, preventDamage);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            } else if (elseEffect instanceof BoostAllOwnCreaturesEffect boost) {
                boostAllOwnCreaturesHandler.resolve(gameData, entry, boost);
            } else if (elseEffect instanceof ReturnToHandEffect returnToHand) {
                returnToHandEffectHandler.resolve(gameData, entry, returnToHand);
            } else {
                var handler = effectHandlerRegistry.getHandler(elseEffect);
                if (handler == null) {
                    log.warn("Game {} - Unsupported ForcedCostOrElse fallback effect: {}",
                            gameData.id, elseEffect.getClass().getSimpleName());
                } else {
                    handler.resolve(gameData, entry, elseEffect);
                    if (gameData.interaction.isAwaitingInput()) {
                        return;
                    }
                }
            }
            // CR 616.1: a caller may open the rerun window for the first fallback effect only.
            gameData.deathReplacementsCanRerunEffect = false;
        }
    }

    private void phaseOutSource(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        phasingService.phaseOut(gameData, List.of(source));
    }

    private void destroySource(GameData gameData, StackEntry entry, boolean cannotBeRegenerated) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        tryDestroyAndLog(gameData, source, entry.getCard().getName(), cannotBeRegenerated);
    }

    private void destroySourceAndDamageControllerIfDestroyed(GameData gameData, StackEntry entry, int damage) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        Permanent lastKnownSource = new Permanent(source);
        lastKnownSource.setCard(permanentRemovalService.snapshotEffectivePermanentCard(gameData, source));
        entry.setSourcePermanentSnapshot(lastKnownSource);
        boolean destroyed = tryDestroyAndLog(gameData, source, entry.getCard().getName());
        if (destroyed) {
            damageSupportProvider.getObject().dealDamageToPlayer(
                    gameData, entry, entry.getControllerId(), damage);
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private void dealDamageToControllerThenTapSourceIfDealt(GameData gameData, StackEntry entry, int damage) {
        UUID controllerId = entry.getControllerId();
        int damageBefore = gameData.damageDealtToPlayersThisTurn.getOrDefault(controllerId, 0);
        damageSupportProvider.getObject().dealDamageToPlayer(gameData, entry, controllerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
        // "If this creature deals damage to you this way, tap it" — prevention/redirect leaves it untapped,
        // while damage that causes no life change (Platinum Emperion) was still dealt.
        if (gameData.damageDealtToPlayersThisTurn.getOrDefault(controllerId, 0) > damageBefore) {
            tapSourcePermanent(gameData, entry);
        }
    }

    private void sacrificeSource(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }
        UUID currentControllerId = gameQueryService.findPermanentController(gameData, self.getId());
        if (!entry.getControllerId().equals(currentControllerId)
                || gameQueryService.cantBeSacrificed(gameData, self)) {
            return;
        }
        if (permanentRemovalService.sacrificePermanentToGraveyard(gameData, self)) {
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
            gameLogService.append(gameData, GameLog.isSacrificed(self.getCard()));
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    private void tapSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent != null) {
            tapUntapSupportProvider.getObject().tapPermanent(gameData, sourcePermanent, entry.getControllerId());
            gameLogService.append(gameData, GameLog.cardThen(sourcePermanent.getCard(), " is tapped."));
            log.info("Game {} - {} is tapped (no matching creature to sacrifice)",
                    gameData.id, sourcePermanent.getCard().getName());
        }
    }

    private void tapEnchantedPermanent(GameData gameData, StackEntry entry) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            aura = entry.getSourcePermanentSnapshot();
        }
        if (aura == null || !aura.isAttached()) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }
        tapUntapSupportProvider.getObject().tapPermanent(gameData, enchanted, entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.cardTextCard(entry.getCard(), " taps ", enchanted.getCard(), "."));
        log.info("Game {} - {} taps enchanted permanent {}",
                gameData.id, entry.getCard().getName(), enchanted.getCard().getName());
    }

    public void applyOpponentsLoseLife(GameData gameData, UUID controllerId, int amount, String sourceName) {
        if (amount <= 0) return;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            lifeSupport.applyLifeLoss(gameData, playerId, amount, sourceName);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    public void createTokenForPlayer(GameData gameData, UUID controllerId,
                                      CreateTokenEffect token, String sourceName, String sourceSetCode) {
        createTokenForPlayer(gameData, controllerId, token, 1, sourceName, sourceSetCode);
    }

    public void createTokenForPlayer(GameData gameData, UUID controllerId,
                                       CreateTokenEffect token, int tokenCount,
                                       String sourceName, String sourceSetCode) {
        boolean baseTokenIsCreature = token.primaryType() == CardType.CREATURE;
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId, baseTokenIsCreature);
        CreateTokenEffect additionalFrog = TokenCreationReplacementSupport.additionalFrogTokenIfApplicable(
                gameData, controllerId, token);
        int additionalSoldierTokenCount = TokenCreationReplacementSupport.additionalSoldierTokenCountIfApplicable(
                gameData, controllerId, token);
        int additionalThopterTokenCount = TokenCreationReplacementSupport.additionalThopterTokenCount(
                gameData, controllerId, token, tokenCount);
        CreateTokenEffect additionalThopterToken = additionalThopterTokenCount > 0
                ? TokenCreationReplacementSupport.additionalThopterToken(token)
                : null;
        CreateTokenEffect additionalSoldier = additionalSoldierTokenCount > 0
                ? TokenCreationReplacementSupport.additionalSoldierTokenIfApplicable(
                        gameData, controllerId, token)
                : null;
        int totalAmount = gameQueryService.getTokenCreationAmount(gameData, controllerId, tokenCount, token.subtypes(), baseTokenIsCreature);
        int additionalFoodTokenCount = TokenCreationReplacementSupport.additionalFoodTokenCount(
                gameData, controllerId, totalAmount);
        CreateTokenEffect additionalFoodToken = additionalFoodTokenCount > 0
                ? TokenCreationReplacementSupport.additionalFoodToken(token)
                : null;
        List<CreateTokenEffect> academyManufactorTokenBlueprints =
                TokenCreationReplacementSupport.academyManufactorTokenBlueprints(
                        gameData, controllerId, token, totalAmount);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        List<CreateTokenEffect> tokenBlueprints = new ArrayList<>();
        if (academyManufactorTokenBlueprints.isEmpty()) {
            for (int count = 0; count < totalAmount; count++) {
                tokenBlueprints.add(token);
            }
        } else {
            tokenBlueprints.addAll(academyManufactorTokenBlueprints);
        }
        if (additionalFrog != null && totalAmount > 0) {
            tokenBlueprints.add(additionalFrog);
        }
        if (totalAmount <= 0) {
            additionalSoldierTokenCount = 0;
        }
        for (int i = 0; i < additionalSoldierTokenCount; i++) {
            tokenBlueprints.add(additionalSoldier);
        }
        for (int i = 0; i < additionalThopterTokenCount; i++) {
            tokenBlueprints.add(additionalThopterToken);
        }
        for (int i = 0; i < additionalFoodTokenCount; i++) {
            tokenBlueprints.add(additionalFoodToken);
        }
        for (CreateTokenEffect tokenToCreate : tokenBlueprints) {
            int tokenPower = tokenToCreate.tokenPower();
            int tokenToughness = tokenToCreate.tokenToughness();
            boolean isCreature = tokenToCreate.primaryType() == CardType.CREATURE;
            Card tokenCard = TokenCardFactory.create(
                    tokenToCreate, tokenPower, tokenToughness, sourceSetCode);
            tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                    gameData, controllerId, tokenCard);

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);
            if (tokenToCreate.tappedAndAttacking()) {
                tokenPermanent.tap();
                tokenPermanent.setAttacking(true);
            } else if (tokenToCreate.tapped()) {
                tokenPermanent.tap();
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            String colorName = tokenToCreate.color() != null ? tokenToCreate.color().name().toLowerCase() + " " : "";
            if (isCreature) {
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " creates a " + tokenPower + "/" + tokenToughness
                                + " " + colorName)
                        .card(tokenCard)
                        .text(" creature token.")
                        .build());
                log.info("Game {} - {} creates a {}/{} {} token for {}", gameData.id, sourceName,
                        tokenPower, tokenToughness, tokenToCreate.tokenName(), playerName);

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            } else {
                gameLogService.append(gameData, GameLog.builder()
                        .text(playerName + " creates a " + colorName)
                        .card(tokenCard)
                        .text(" token.")
                        .build());
                log.info("Game {} - {} creates a {} token for {}", gameData.id, sourceName,
                        tokenToCreate.tokenName(), playerName);
            }
            if (tokenToCreate.investigate()) {
                triggerCollectionService.checkInvestigateTriggers(gameData, controllerId);
            }
        }
    }

    public void completePileSeparationStep1(GameData gameData, List<UUID> pile1Ids) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        UUID targetPlayerId = state.targetPlayerId();

        List<UUID> pile1 = new ArrayList<>(state.pile1Ids());
        pile1.addAll(pile1Ids);
        // Pile 2 is everything not in pile 1
        List<UUID> pile2 = new ArrayList<>(state.pile2Ids());
        for (UUID permId : state.allPermanentIds()) {
            if (!pile1Ids.contains(permId)) {
                pile2.add(permId);
            }
        }

        // Re-queue with the piles filled — step 2 (the pile-choice may prompt) polls it.
        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), targetPlayerId,
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2, state.disposition()));

        // Build pile descriptions for the prompt
        String pile1Desc = buildPileDescription(gameData, pile1);
        String pile2Desc = buildPileDescription(gameData, pile2);

        String controllerName = gameData.playerIdToName.get(state.controllerId());
        gameLogService.append(gameData, GameLog.text(controllerName + " separates permanents into two piles. Pile 1: " + pile1Desc + ". Pile 2: " + pile2Desc + "."));

        String action = state.disposition() == CardPileDisposition.DESTROY ? "destroy" : "sacrifice";
        String prompt = "Choose a pile to " + action + ". Yes = Pile 1 (" + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, targetPlayerId, List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    public void completePileSeparationStep2(GameData gameData, boolean accepted) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        List<UUID> pileToSacrifice = accepted
                ? new ArrayList<>(state.pile1Ids())
                : new ArrayList<>(state.pile2Ids());
        String pileName = accepted ? "Pile 1" : "Pile 2";

        UUID targetPlayerId = state.targetPlayerId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        String pileDescription = buildPileDescription(gameData, pileToSacrifice);
        if (state.disposition() == CardPileDisposition.DESTROY) {
            gameLogService.append(gameData, GameLog.text(playerName + " destroys " + pileName + ": " + pileDescription + "."));

            List<Permanent> creaturesToDestroy = pileToSacrifice.stream()
                    .map(permId -> gameQueryService.findPermanentById(gameData, permId))
                    .filter(perm -> perm != null && gameQueryService.isCreature(gameData, perm))
                    .toList();
            destroyBatch(gameData, creaturesToDestroy, "Do or Die", true);
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " sacrifices " + pileName + ": " + pileDescription + "."));

            List<DepartureIntent> intents = pileToSacrifice.stream()
                    .filter(permId -> gameQueryService.findPermanentById(gameData, permId) != null)
                    .distinct()
                    .map(DepartureIntent::sacrifice)
                    .toList();
            departureService.departNow(gameData, DepartureBatch.of(intents), DepartureService.Hooks.afterEach(
                    (intent, perm, snapshot, removed) -> {
                        if (removed) {
                            gameData.recordSacrificedPermanent(targetPlayerId, perm.getCard());
                        }
                    }));
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    private String buildPileDescription(GameData gameData, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                names.add(perm.getCard().getName());
            }
        }
        return String.join(", ", names);
    }
}
