package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsControlOfSourceCreatureEffect;
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
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
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
    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
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
    private final GrantEffectToSourceUntilEndOfCombatEffectHandler grantEffectToSourceUntilEndOfCombatHandler;
    private final PreventDamageFromChosenSourceEffectHandler preventDamageFromChosenSourceHandler;
    private final BoostAllOwnCreaturesEffectHandler boostAllOwnCreaturesHandler;
    private final BounceSupport bounceSupport;
    private final EnergyCountersEffectHandler energyCountersEffectHandler;
    private final DrawCardEffectHandler drawCardEffectHandler;

    public void beginNextDestroyRestChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                           List<UUID> protectedIds, String sourceName) {
        if (choosers.isEmpty()) return;
        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remainingChoosers = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.DestroyRestChoice(remainingChoosers, List.copyOf(protectedIds), sourceName),
                "Choose a creature to keep. The rest will be destroyed.");
    }

    public void completeDestroyRestChoice(GameData gameData, List<UUID> permanentIds,
                                          MultiPermanentChoiceContext.DestroyRestChoice context) {
        // Add the chosen creature to the protected set
        List<UUID> protectedIds = new ArrayList<>(context.protectedIds());
        protectedIds.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            // More players need to choose — prompt the next one
            beginNextDestroyRestChoice(gameData, context.remainingChoosers(), protectedIds, context.sourceName());
            return;
        }

        // All players have chosen — destroy all non-protected creatures
        String sourceName = context.sourceName();
        performDestroyAllCreaturesExcept(gameData, sourceName != null ? sourceName : "unknown", protectedIds);
    }

    public void performDestroyAllCreaturesExcept(GameData gameData, String sourceName, List<UUID> protectedIdList) {
        Set<UUID> protectedIds = new HashSet<>(protectedIdList);

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !protectedIds.contains(perm.getId())) {
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
                if (perm.getCard().hasType(CardType.LAND)) {
                    continue;
                }
                if (perm.getCard().getManaValue() == targetManaValue) {
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
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        List<Permanent> actuallyDying = new ArrayList<>();
        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                continue;
            }
            if (graveyardService.tryReplaceDestruction(gameData, perm, !cannotBeRegenerated)) {
                continue;
            }
            actuallyDying.add(perm);
        }

        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameLogService.append(gameData, GameLog.isIndestructible(perm.getCard()));
            }
        }

        try {
            beginSimultaneousCreatureDeaths(gameData, actuallyDying);
            for (Permanent perm : actuallyDying) {
                permanentRemovalService.destroyPermanentToGraveyard(gameData, perm);
                gameLogService.append(gameData, GameLog.isDestroyed(perm.getCard()));
                log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
            }
        } finally {
            endSimultaneousCreatureDeaths(gameData);
        }
        return actuallyDying;
    }

    private void beginSimultaneousCreatureDeaths(GameData gameData, List<Permanent> dying) {
        for (Permanent perm : dying) {
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
            if (controllerId == null) continue;
            gameData.simultaneousDyingCreatures.put(perm.getId(), perm);
            gameData.simultaneousDyingControllers.put(perm.getId(), controllerId);
        }
    }

    private void endSimultaneousCreatureDeaths(GameData gameData) {
        gameData.simultaneousDyingCreatures.clear();
        gameData.simultaneousDyingControllers.clear();
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        if (!permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated)) {
            return false;
        }
        gameLogService.append(gameData, GameLog.isDestroyed(target.getCard()));
        log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), sourceName);
        return true;
    }

    public void sacrificeAndLog(GameData gameData, Permanent creature, UUID playerId) {
        Card sacrificedCard = creature.getCard();
        permanentRemovalService.removePermanentToGraveyard(gameData, creature);
        gameData.playersWhoSacrificedPermanentsThisTurn.add(playerId);
        gameData.recordSacrificedPermanent(playerId, sacrificedCard);
        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.playerSacrifices(playerName, sacrificedCard));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, sacrificedCard.getName());
        // Collect both ally-permanent-sacrificed and global creature-sacrificed triggers for the
        // edict / chosen / forced-sacrifice paths that funnel through this shared helper.
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, playerId, sacrificedCard);
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
            for (Permanent perm : matching) {
                sacrificeAndLog(gameData, perm, playerId);
            }
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
                .toList();

        if (matching.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no matching permanents to destroy."));
            return false;
        }

        if (matching.size() <= count) {
            for (Permanent perm : matching) {
                tryDestroyAndLog(gameData, perm, sourceName);
            }
            permanentRemovalService.removeOrphanedAuras(gameData);
            return false;
        }

        List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
        playerInputService.beginMultiPermanentChoice(gameData, playerId, matchingIds, count,
                new MultiPermanentChoiceContext.ForcedDestroy(playerId, sourceName),
                "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to destroy.");
        return true;
    }

    public void dealNoncombatDamageToPlayer(GameData gameData, UUID playerId, int baseDamage,
                                              String cardName, Card sourceCard) {
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage);

        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isDamageFromCardSourcePrevented(gameData, sourceCard)
                    || damagePreventionService.applyColorDamagePreventionForPlayer(
                            gameData, playerId, sourceCard.getColor()))) {
            gameLogService.append(gameData, GameLog.text(cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }

        int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, damage);
        effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
        effectiveDamage -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                gameData, playerId, effectiveDamage);

        if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
            if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                int poisonAmount = gameQueryService.replacePoisonCounters(gameData, playerId, effectiveDamage);
                if (poisonAmount > 0) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + poisonAmount);
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameLogService.append(gameData, GameLog.text(playerName + " gets " + poisonAmount + " poison counters from " + cardName + "."));
                }
            }
            lifeSupport.applyPoisonCounters(gameData, playerId, effectiveDamage, cardName,
                    gameData.currentlyResolvingControllerId);
            return;
        }

        if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + "'s life total can't change."));
            return;
        }

        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

        if (effectiveDamage > 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(cardName + " deals " + effectiveDamage + " damage to " + playerName + "."));
            log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, effectiveDamage, playerName);
        }
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
     * Removes one counter from {@code permanent}, taking the first kind present when it carries
     * several. Returns false when the permanent has no counters left to remove.
     */
    public boolean removeOneCounterAndLog(GameData gameData, Permanent permanent, UUID playerId) {
        CounterType kind = permanent.getCounters().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (kind == null) {
            return false;
        }
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

    public void performSimultaneousSacrifice(GameData gameData, List<UUID> ids) {
        for (UUID permId : ids) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                sacrificeAndLog(gameData, perm, controllerId);
            }
        }
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds) {
        beginNextForcedSacrificeFromQueue(gameData, choosers, accumulatedSacrificeIds, false);
    }

    public void beginNextForcedSacrificeFromQueue(GameData gameData, List<PendingForcedSacrifice> choosers,
                                                  List<UUID> accumulatedSacrificeIds, boolean simultaneousFlow) {
        if (choosers.isEmpty()) {
            return;
        }

        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remainingChoosers = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.ForcedSacrifice(next.playerId(), remainingChoosers,
                        List.copyOf(accumulatedSacrificeIds), simultaneousFlow),
                "Choose " + next.count() + " permanent"
                        + (next.count() > 1 ? "s" : "") + " to sacrifice.");
    }

    public void performSacrificeCreatureForPlayer(GameData gameData, UUID targetPlayerId) {
        List<UUID> creatureIds = collectCreatureIds(gameData, targetPlayerId,
                p -> !gameQueryService.cantBeSacrificed(gameData, p));

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
                dealNoncombatDamageToPlayer(gameData, entry.getControllerId(), fixed.value(),
                        entry.getCard().getName(), entry.getEffectiveDamageSourceCard());
                gameOutcomeService.checkWinCondition(gameData);
            } else if (elseEffect instanceof DealDamageToPlayersEffect damage
                    && damage.recipient() == DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER
                    && damage.amount() instanceof Fixed fixed) {
                // Mind Whip: damage the enchanted permanent's controller (baked as targetId).
                UUID victim = entry.getTargetId();
                if (victim != null) {
                    dealNoncombatDamageToPlayer(gameData, victim, fixed.value(),
                            entry.getCard().getName(), entry.getEffectiveDamageSourceCard());
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
                    && poison.recipient() == PoisonRecipient.CONTROLLER) {
                // "unless they pay {2}, they get another poison counter" (Sabertooth Cobra) — the
                // entry controller is the player who owes the payment.
                lifeSupport.applyPoisonCounters(gameData, entry.getControllerId(), poison.amount(),
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
            } else if (elseEffect instanceof com.github.laxika.magicalvibes.model.effect.ExileControllerLibraryEffect) {
                // "When a player doesn't pay this enchantment's cumulative upkeep, that player
                // exiles all cards from their library" (Thought Lash).
                libraryExileSupport.exileEntireLibrary(gameData, entry.getControllerId());
            } else if (elseEffect instanceof RemoveAllCountersEffect removeCounters
                    && removeCounters.subject() == CounterRemovalSubject.SOURCE) {
                // "remove all wage counters from this creature" (Rogue Skycaptain).
                removeAllCountersHandler.resolve(gameData, entry, removeCounters);
            } else if (elseEffect instanceof GrantEffectToSourceUntilEndOfCombatEffect grant) {
                grantEffectToSourceUntilEndOfCombatHandler.resolve(gameData, entry, grant);
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
                log.warn("Game {} - Unsupported ForcedCostOrElse fallback effect: {}",
                        gameData.id, elseEffect.getClass().getSimpleName());
            }
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
        boolean destroyed = tryDestroyAndLog(gameData, source, entry.getCard().getName());
        if (destroyed) {
            dealNoncombatDamageToPlayer(gameData, entry.getControllerId(), damage,
                    entry.getCard().getName(), entry.getEffectiveDamageSourceCard());
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private void dealDamageToControllerThenTapSourceIfDealt(GameData gameData, StackEntry entry, int damage) {
        UUID controllerId = entry.getControllerId();
        int lifeBefore = gameData.getLife(controllerId);
        dealNoncombatDamageToPlayer(gameData, controllerId, damage,
                entry.getCard().getName(), entry.getEffectiveDamageSourceCard());
        gameOutcomeService.checkWinCondition(gameData);
        // "If this creature deals damage to you this way, tap it" — prevention/redirect leaves it untapped.
        if (gameData.getLife(controllerId) < lifeBefore) {
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
        if (permanentRemovalService.removePermanentToGraveyard(gameData, self)) {
            triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, entry.getControllerId(), self.getCard());
            gameLogService.append(gameData, GameLog.isSacrificed(self.getCard()));
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    private void tapSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent != null) {
            sourcePermanent.tap();
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
        enchanted.tap();
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
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        boolean isCreature = token.primaryType() == CardType.CREATURE;

        for (int count = 0; count < tokenCount; count++) {
            for (int copy = 0; copy < tokenMultiplier; copy++) {
                Card tokenCard = TokenCardFactory.create(
                        token, token.tokenPower(), token.tokenToughness(), sourceSetCode);
                tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                        gameData, controllerId, tokenCard);

                Permanent tokenPermanent = new Permanent(tokenCard);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);
                if (token.tappedAndAttacking()) {
                    tokenPermanent.tap();
                    tokenPermanent.setAttacking(true);
                } else if (token.tapped()) {
                    tokenPermanent.tap();
                }

                String playerName = gameData.playerIdToName.get(controllerId);
                String colorName = token.color() != null ? token.color().name().toLowerCase() + " " : "";
                if (isCreature) {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(playerName + " creates a " + token.tokenPower() + "/" + token.tokenToughness()
                                    + " " + colorName)
                            .card(tokenCard)
                            .text(" creature token.")
                            .build());
                    log.info("Game {} - {} creates a {}/{} {} token for {}", gameData.id, sourceName,
                            token.tokenPower(), token.tokenToughness(), token.tokenName(), playerName);

                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                } else {
                    gameLogService.append(gameData, GameLog.builder()
                            .text(playerName + " creates a " + colorName)
                            .card(tokenCard)
                            .text(" token.")
                            .build());
                    log.info("Game {} - {} creates a {} token for {}", gameData.id, sourceName,
                            token.tokenName(), playerName);
                }
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

            for (UUID permId : pileToSacrifice) {
                Permanent perm = gameQueryService.findPermanentById(gameData, permId);
                if (perm != null) {
                    if (permanentRemovalService.removePermanentToGraveyard(gameData, perm)) {
                        gameData.recordSacrificedPermanent(targetPlayerId, perm.getCard());
                    }
                }
            }
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
