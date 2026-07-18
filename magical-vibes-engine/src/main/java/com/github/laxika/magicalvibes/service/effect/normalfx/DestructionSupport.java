package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourceAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService predicateEvaluationService;
    private final LifeSupport lifeSupport;

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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + " resolves but no creatures are destroyed."));
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} finds no nonland permanents with mana value {}", gameData.id, cardName, targetManaValue);
            return;
        }

        destroyBatch(gameData, toDestroy, cardName, false);
    }

    /** @return the number of permanents actually destroyed (indestructible / regenerated don't count) */
    public int destroyBatch(GameData gameData, List<Permanent> toDestroy, String sourceName,
                              boolean cannotBeRegenerated) {
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        int destroyedCount = 0;
        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " is indestructible."));
                continue;
            }
            if (!cannotBeRegenerated && graveyardService.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName() + " is destroyed."));
            log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
            destroyedCount++;
        }
        return destroyedCount;
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        if (!permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated)) {
            return false;
        }
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), sourceName);
        return true;
    }

    public void sacrificeAndLog(GameData gameData, Permanent creature, UUID playerId) {
        permanentRemovalService.removePermanentToGraveyard(gameData, creature);
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no matching permanents to destroy."));
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
                                              String cardName, CardColor sourceColor) {
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage);

        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                    || damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, sourceColor))) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }

        int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, damage);
        effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

        if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
            if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " gets " + effectiveDamage + " poison counters from " + cardName + "."));
            }
            return;
        }

        if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + "'s life total can't change."));
            return;
        }

        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

        if (effectiveDamage > 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + " deals " + effectiveDamage + " damage to " + playerName + "."));
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
        if (choosers.isEmpty()) {
            return;
        }

        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remainingChoosers = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.ForcedSacrifice(next.playerId(), remainingChoosers,
                        List.copyOf(accumulatedSacrificeIds)),
                "Choose " + next.count() + " permanent"
                        + (next.count() > 1 ? "s" : "") + " to sacrifice.");
    }

    public void performSacrificeCreatureForPlayer(GameData gameData, UUID targetPlayerId) {
        List<UUID> creatureIds = collectCreatureIds(gameData, targetPlayerId, p -> true);

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
            return;
        }

        sacrificeAndLog(gameData, target, context.controllerId());
    }

    public void resolveForcedCostElseEffects(GameData gameData, StackEntry entry, ForcedCostOrElseEffect effect) {
        for (var elseEffect : effect.elseEffects()) {
            if (elseEffect instanceof TapPermanentsEffect tap && tap.scope() == TapUntapScope.SELF) {
                tapSourcePermanent(gameData, entry);
            } else if (elseEffect instanceof DealDamageToPlayersEffect damage
                    && damage.recipient() == DamageRecipient.CONTROLLER
                    && damage.amount() instanceof Fixed fixed) {
                dealNoncombatDamageToPlayer(gameData, entry.getControllerId(), fixed.value(),
                        entry.getCard().getName(), entry.getCard().getColor());
                gameOutcomeService.checkWinCondition(gameData);
            } else if (elseEffect instanceof SacrificeSelfEffect) {
                sacrificeSource(gameData, entry);
            } else if (elseEffect instanceof LoseLifeEffect loseLife
                    && loseLife.recipient() == LoseLifeRecipient.CONTROLLER
                    && loseLife.amount() instanceof Fixed lifeAmount) {
                // "unless you pay {cost}, you lose N life" (Nafs Asp). Life loss, not damage
                // (CR 118.2) — never routed through damage plumbing.
                lifeSupport.applyLifeLoss(gameData, entry.getControllerId(), lifeAmount.value(), entry.getCard().getName());
                gameOutcomeService.checkWinCondition(gameData);
            } else if (elseEffect instanceof DestroySourceAndDamageControllerIfDestroyedEffect destroyDamage) {
                destroySourceAndDamageControllerIfDestroyed(gameData, entry, destroyDamage.damage());
            } else {
                log.warn("Game {} - Unsupported ForcedCostOrElse fallback effect: {}",
                        gameData.id, elseEffect.getClass().getSimpleName());
            }
        }
    }

    private void destroySourceAndDamageControllerIfDestroyed(GameData gameData, StackEntry entry, int damage) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        boolean destroyed = tryDestroyAndLog(gameData, source, entry.getCard().getName());
        if (destroyed) {
            dealNoncombatDamageToPlayer(gameData, entry.getControllerId(), damage,
                    entry.getCard().getName(), entry.getCard().getColor());
            gameOutcomeService.checkWinCondition(gameData);
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(self.getCard().getName() + " is sacrificed."));
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    private void tapSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent != null) {
            sourcePermanent.tap();
            String tapLog = sourcePermanent.getCard().getName() + " is tapped.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(tapLog));
            log.info("Game {} - {} is tapped (no matching creature to sacrifice)",
                    gameData.id, sourcePermanent.getCard().getName());
        }
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
                                      CreateTokenEffect token, String sourceName) {
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        boolean isCreature = token.primaryType() == CardType.CREATURE;

        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = new Card();
            tokenCard.setName(token.tokenName());
            tokenCard.setType(token.primaryType());
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(token.color());
            if (isCreature) {
                tokenCard.setPower(token.tokenPower());
                tokenCard.setToughness(token.tokenToughness());
            }
            tokenCard.setSubtypes(token.subtypes());
            if (token.keywords() != null && !token.keywords().isEmpty()) {
                tokenCard.setKeywords(token.keywords());
            }
            if (token.additionalTypes() != null && !token.additionalTypes().isEmpty()) {
                tokenCard.setAdditionalTypes(token.additionalTypes());
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String playerName = gameData.playerIdToName.get(controllerId);
            String colorName = token.color() != null ? token.color().name().toLowerCase() + " " : "";
            if (isCreature) {
                String logEntry = playerName + " creates a " + token.power() + "/" + token.toughness()
                        + " " + colorName + token.tokenName() + " creature token.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} creates a {}/{} {} token for {}", gameData.id, sourceName,
                        token.power(), token.toughness(), token.tokenName(), playerName);

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            } else {
                String logEntry = playerName + " creates a " + colorName + token.tokenName() + " token.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} creates a {} token for {}", gameData.id, sourceName,
                        token.tokenName(), playerName);
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
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2));

        // Build pile descriptions for the prompt
        String pile1Desc = buildPileDescription(gameData, pile1);
        String pile2Desc = buildPileDescription(gameData, pile2);

        String controllerName = gameData.playerIdToName.get(state.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " separates permanents into two piles. Pile 1: " + pile1Desc + ". Pile 2: " + pile2Desc + "."));

        // Prompt target player to choose which pile to sacrifice
        String prompt = "Choose a pile to sacrifice. Yes = Pile 1 (" + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
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

        String sacrificedDesc = buildPileDescription(gameData, pileToSacrifice);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " sacrifices " + pileName + ": " + sacrificedDesc + "."));

        // Sacrifice all permanents in the chosen pile
        for (UUID permId : pileToSacrifice) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, perm);
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
