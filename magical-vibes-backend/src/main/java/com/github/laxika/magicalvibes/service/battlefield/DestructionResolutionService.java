package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsAndGainLifePerDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGiveControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyOneOfTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureDestroyRestEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SeparatePermanentsIntoPilesAndSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Resolves destruction and sacrifice effects from the stack.
 *
 * <p>Handles targeted destruction, mass destruction (board wipes), equipment destruction,
 * creature sacrifice (including opponent-forced sacrifices), and compound effects that
 * combine destruction with life loss, life gain, damage, or power boosts.
 * All methods respect indestructible, regeneration, and damage prevention rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DestructionResolutionService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final LifeResolutionService lifeResolutionService;

    /**
     * Resolves a {@link DestroyAllPermanentsEffect}, destroying all permanents matching the
     * effect's predicate filter. Respects indestructible and regeneration (unless
     * {@code cannotBeRegenerated} is set).
     */
    @HandlesEffect(DestroyAllPermanentsEffect.class)
    void resolveDestroyAllPermanents(GameData gameData, StackEntry entry, DestroyAllPermanentsEffect effect) {
        List<Permanent> toDestroy = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(perm, effect.filter(), filterContext)) {
                    toDestroy.add(perm);
                }
            }
        });

        destroyBatch(gameData, toDestroy, entry.getCard().getName(), effect.cannotBeRegenerated());
    }

    /**
     * Resolves a {@link DestroyAllPermanentsAndGainLifePerDestroyedEffect}, destroying all
     * permanents matching the filter and granting the controller life for each actually destroyed.
     * Indestructible and regenerated permanents do not count toward the life gain.
     */
    @HandlesEffect(DestroyAllPermanentsAndGainLifePerDestroyedEffect.class)
    void resolveDestroyAllPermanentsAndGainLifePerDestroyed(
            GameData gameData, StackEntry entry,
            DestroyAllPermanentsAndGainLifePerDestroyedEffect effect) {
        List<Permanent> toDestroy = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(perm, effect.filter(), filterContext)) {
                    toDestroy.add(perm);
                }
            }
        });

        // Snapshot indestructible before any removals
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        // Destroy and count
        int destroyedCount = 0;
        String sourceName = entry.getCard().getName();
        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is indestructible.");
                continue;
            }
            if (graveyardService.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is destroyed.");
            log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
            destroyedCount++;
        }

        // Gain life for each destroyed permanent
        if (destroyedCount > 0) {
            int totalLife = destroyedCount * effect.lifePerDestroyed();
            lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), totalLife, sourceName);
        }
    }

    /**
     * Resolves a {@link EachPlayerChoosesCreatureDestroyRestEffect}. Each player in APNAP order
     * chooses a creature they control; all other creatures are destroyed simultaneously.
     * Players with zero or one creature auto-resolve.
     */
    @HandlesEffect(EachPlayerChoosesCreatureDestroyRestEffect.class)
    void resolveEachPlayerChoosesCreatureDestroyRest(GameData gameData, StackEntry entry) {
        gameData.pendingDestroyRestMode = true;
        gameData.pendingDestroyRestProtectedIds.clear();
        gameData.pendingDestroyRestSourceName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> creatures = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null) {
                for (Permanent perm : battlefield) {
                    if (gameQueryService.isCreature(gameData, perm)) {
                        creatures.add(perm);
                    }
                }
            }

            if (creatures.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, playerName + " has no creatures.");
                continue;
            }

            if (creatures.size() == 1) {
                // Auto-keep the only creature
                gameData.pendingDestroyRestProtectedIds.add(creatures.getFirst().getId());
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " keeps " + creatures.getFirst().getCard().getName() + " (only creature).");
                continue;
            }

            // Multiple creatures — player must choose 1 to keep
            List<UUID> creatureIds = creatures.stream().map(Permanent::getId).toList();
            gameData.pendingForcedSacrificeQueue.add(
                    new PendingForcedSacrifice(playerId, 1, creatureIds));
        }

        if (gameData.pendingForcedSacrificeQueue.isEmpty()) {
            // All auto-resolved — destroy non-protected creatures now
            performDestroyAllCreaturesExcept(gameData, entry.getCard().getName());
        } else {
            beginNextDestroyRestChoice(gameData);
        }
    }

    /**
     * Prompts the next player in the destroy-rest queue to choose a creature to keep.
     */
    void beginNextDestroyRestChoice(GameData gameData) {
        if (gameData.pendingForcedSacrificeQueue.isEmpty()) return;
        PendingForcedSacrifice next = gameData.pendingForcedSacrificeQueue.removeFirst();
        gameData.pendingForcedSacrificeCount = next.count();
        gameData.pendingForcedSacrificePlayerId = next.playerId();
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(), "Choose a creature to keep. The rest will be destroyed.");
    }

    /**
     * Completes a player's "choose creature to keep" selection during a destroy-rest flow.
     * Called from {@link com.github.laxika.magicalvibes.service.input.MultiPermanentChoiceHandlerService}.
     *
     * @param gameData     the current game state
     * @param permanentIds the permanent IDs the player chose to keep
     */
    public void completeDestroyRestChoice(GameData gameData, List<UUID> permanentIds) {
        gameData.pendingForcedSacrificeCount = 0;
        gameData.pendingForcedSacrificePlayerId = null;

        // Add the chosen creature to the protected set
        gameData.pendingDestroyRestProtectedIds.addAll(permanentIds);

        if (!gameData.pendingForcedSacrificeQueue.isEmpty()) {
            // More players need to choose — prompt the next one
            beginNextDestroyRestChoice(gameData);
            return;
        }

        // All players have chosen — destroy all non-protected creatures
        String sourceName = gameData.pendingDestroyRestSourceName;
        performDestroyAllCreaturesExcept(gameData, sourceName != null ? sourceName : "unknown");
    }

    /**
     * Destroys all creatures not in the protected set. Clears destroy-rest state afterwards.
     */
    private void performDestroyAllCreaturesExcept(GameData gameData, String sourceName) {
        Set<UUID> protectedIds = new HashSet<>(gameData.pendingDestroyRestProtectedIds);
        gameData.pendingDestroyRestProtectedIds.clear();
        gameData.pendingDestroyRestMode = false;

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm) && !protectedIds.contains(perm.getId())) {
                    toDestroy.add(perm);
                }
            }
        });

        if (toDestroy.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, sourceName + " resolves but no creatures are destroyed.");
            return;
        }

        destroyBatch(gameData, toDestroy, sourceName, false);
    }

    /**
     * Resolves a {@link DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect}, destroying
     * all creatures and then creating a single colorless creature token whose power and toughness
     * equal the number of creatures actually destroyed (excluding indestructible and regenerated).
     */
    @HandlesEffect(DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect.class)
    void resolveDestroyAllCreaturesAndCreateTokenFromDestroyedCount(
            GameData gameData, StackEntry entry,
            DestroyAllCreaturesAndCreateTokenFromDestroyedCountEffect effect) {
        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    toDestroy.add(perm);
                }
            }
        });

        // Snapshot indestructible before any removals
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        // Destroy and count
        int destroyedCount = 0;
        String sourceName = entry.getCard().getName();
        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is indestructible.");
                continue;
            }
            if (graveyardService.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is destroyed.");
            log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
            destroyedCount++;
        }

        // Create token with P/T = destroyed count (doubled by token creation replacement effects)
        UUID controllerId = entry.getControllerId();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));

        for (int copy = 0; copy < tokenMultiplier; copy++) {
            Card tokenCard = new Card();
            tokenCard.setName(effect.tokenName());
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(null);
            tokenCard.setPower(destroyedCount);
            tokenCard.setToughness(destroyedCount);
            tokenCard.setSubtypes(effect.tokenSubtypes());
            if (effect.tokenAdditionalTypes() != null && !effect.tokenAdditionalTypes().isEmpty()) {
                tokenCard.setAdditionalTypes(effect.tokenAdditionalTypes());
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

            String logEntry = "A " + destroyedCount + "/" + destroyedCount + " " + effect.tokenName()
                    + " artifact creature token enters the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} creates a {}/{} {} artifact creature token",
                    gameData.id, sourceName, destroyedCount, destroyedCount, effect.tokenName());

            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
        }
    }

    /**
     * Resolves a {@link DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect},
     * destroying all nonland permanents whose mana value equals the stack entry's X value
     * (derived from charge counters).
     */
    @HandlesEffect(DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect.class)
    void resolveDestroyNonlandPermanentsByChargeCounterManaValue(GameData gameData, StackEntry entry) {
        destroyNonlandPermanentsByManaValue(gameData, entry.getXValue(), entry.getCard().getName(), null);
    }

    /**
     * Resolves a {@link DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect},
     * destroying all nonland permanents whose mana value equals X, but only those controlled
     * by players who were dealt combat damage by the source this turn. Fizzles if the source
     * dealt no combat damage this turn.
     */
    @HandlesEffect(DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect.class)
    void resolveDestroyNonlandPermanentsByManaValueXDealtCombatDamage(GameData gameData, StackEntry entry) {
        String cardName = entry.getCard().getName();

        Set<UUID> damagedPlayerIds = gameData.combatDamageToPlayersThisTurn
                .getOrDefault(entry.getSourcePermanentId(), Set.of());
        if (damagedPlayerIds.isEmpty()) {
            String logEntry = cardName + " resolves but " + cardName + " dealt no combat damage to any player this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} resolves but dealt no combat damage to any player this turn", gameData.id, cardName);
            return;
        }

        destroyNonlandPermanentsByManaValue(gameData, entry.getXValue(), cardName, damagedPlayerIds);
    }

    /**
     * Shared logic for destroying all nonland permanents with a given mana value.
     *
     * @param playerFilter if non-null, only permanents controlled by players in this set are considered
     */
    private void destroyNonlandPermanentsByManaValue(GameData gameData, int targetManaValue,
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} finds no nonland permanents with mana value {}", gameData.id, cardName, targetManaValue);
            return;
        }

        destroyBatch(gameData, toDestroy, cardName, false);
    }

    /**
     * Simultaneously destroys a batch of permanents, respecting indestructible (snapshotted before
     * any removals) and optional regeneration.
     */
    private void destroyBatch(GameData gameData, List<Permanent> toDestroy, String sourceName,
                              boolean cannotBeRegenerated) {
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is indestructible.");
                continue;
            }
            if (!cannotBeRegenerated && graveyardService.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is destroyed.");
            log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
        }
    }

    private boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    private boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        if (!permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated)) {
            return false;
        }
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), sourceName);
        return true;
    }

    private void sacrificeAndLog(GameData gameData, Permanent creature, UUID playerId) {
        permanentRemovalService.removePermanentToGraveyard(gameData, creature);
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
    }


    /**
     * Deals noncombat damage to a player with full prevention pipeline
     * (source prevention, color prevention, shields, redirection, life change check).
     */
    private void dealNoncombatDamageToPlayer(GameData gameData, UUID playerId, int baseDamage,
                                              String cardName, CardColor sourceColor) {
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage);

        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                    || damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, sourceColor))) {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }

        int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, damage);
        effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

        if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
            if (gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " gets " + effectiveDamage + " poison counters from " + cardName + ".");
            }
            return;
        }

        if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + "'s life total can't change.");
            return;
        }

        int currentLife = gameData.getLife(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

        if (effectiveDamage > 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + " deals " + effectiveDamage + " damage to " + playerName + ".");
            log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, effectiveDamage, playerName);
        }
    }

    private List<UUID> collectCreatureIds(GameData gameData, UUID playerId, Predicate<Permanent> additionalFilter) {
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

    /**
     * Resolves a {@link DestroyTargetPermanentEffect}, destroying the targeted permanent.
     * Respects the effect's {@code cannotBeRegenerated} flag. If {@code tokenForController}
     * is non-null, creates a creature token for the target's controller regardless of
     * whether the destruction succeeds (e.g. Beast Within).
     */
    @HandlesEffect(DestroyTargetPermanentEffect.class)
    void resolveDestroyTargetPermanent(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Capture the controller before destruction (needed for token creation)
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

        tryDestroyAndLog(gameData, target, entry.getCard().getName(), destroy.cannotBeRegenerated());

        // Create token for the target's controller if specified
        if (destroy.tokenForController() != null && controllerId != null) {
            createTokenForPlayer(gameData, controllerId, destroy.tokenForController(), entry.getCard().getName());
        }
    }

    /**
     * Resolves a {@link DestroyOneOfTargetsAtRandomEffect}, picking one permanent at random
     * from {@code targetIds} and destroying it. Targets that have left the battlefield
     * are removed before the random selection.
     */
    @HandlesEffect(DestroyOneOfTargetsAtRandomEffect.class)
    void resolveDestroyOneOfTargetsAtRandom(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }

        // Filter to still-valid targets (still on the battlefield)
        List<UUID> validTargetIds = new ArrayList<>();
        for (UUID targetId : targetIds) {
            if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                validTargetIds.add(targetId);
            }
        }

        if (validTargetIds.isEmpty()) {
            log.info("Game {} - {} random destroy fizzles — all targets have left the battlefield",
                    gameData.id, entry.getCard().getName());
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(validTargetIds.size());
        UUID chosenId = validTargetIds.get(randomIndex);
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenId);
        if (chosen != null) {
            tryDestroyAndLog(gameData, chosen, entry.getCard().getName(), false);
        }
    }

    /**
     * Resolves a {@link DestroyRandomOpponentPermanentWithCounterEffect}. Re-checks the
     * intervening-if condition (at least {@code minRequired} opponent permanents have the
     * specified counter type), then destroys one of those permanents at random.
     */
    @HandlesEffect(DestroyRandomOpponentPermanentWithCounterEffect.class)
    void resolveDestroyRandomOpponentPermanentWithCounter(GameData gameData, StackEntry entry,
                                                           DestroyRandomOpponentPermanentWithCounterEffect effect) {
        UUID controllerId = entry.getControllerId();
        CounterType counterType = effect.counterType();

        // Find all permanents opponents control with the specified counter
        List<Permanent> candidates = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                int counterCount = switch (counterType) {
                    case AIM -> perm.getAimCounters();
                    case CHARGE -> perm.getChargeCounters();
                    default -> 0;
                };
                if (counterCount > 0) {
                    candidates.add(perm);
                }
            }
        }

        // Re-check intervening-if: need at least minRequired permanents with counters
        if (candidates.size() < effect.minRequired()) {
            log.info("Game {} - {} end step trigger fizzles — only {} permanents with {} counters (need {})",
                    gameData.id, entry.getCard().getName(), candidates.size(),
                    counterType.name().toLowerCase(), effect.minRequired());
            return;
        }

        // Destroy one at random
        int randomIndex = ThreadLocalRandom.current().nextInt(candidates.size());
        Permanent chosen = candidates.get(randomIndex);
        tryDestroyAndLog(gameData, chosen, entry.getCard().getName(), false);
    }

    /**
     * Resolves a {@link DestroyEquipmentAttachedToTargetCreatureEffect}, finding and destroying
     * all Equipment attached to the targeted creature.
     */
    @HandlesEffect(DestroyEquipmentAttachedToTargetCreatureEffect.class)
    void resolveDestroyEquipmentAttachedToTargetCreature(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        List<Permanent> equipmentToDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (targetId.equals(p.getAttachedTo())
                    && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                equipmentToDestroy.add(p);
            }
        });

        for (Permanent equipment : equipmentToDestroy) {
            tryDestroyAndLog(gameData, equipment, entry.getCard().getName());
        }
    }

    /**
     * Resolves a {@link DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect} by recording
     * the target creature's ID for end-of-combat equipment destruction. At end of combat,
     * all Equipment currently attached to the recorded creature will be destroyed.
     */
    @HandlesEffect(DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect.class)
    void resolveDestroyEquipmentOnEquippedCombatOpponentAtEndOfCombat(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            gameData.creaturesWithEquipmentToDestroyAtEndOfCombat.add(targetId);
            String logEntry = "Equipment attached to " + target.getCard().getName()
                    + " will be destroyed at end of combat.";
            gameData.gameLog.add(logEntry);
        }
    }

    /**
     * Resolves a {@link DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect}, destroying
     * the targeted creature and then causing its controller to lose life equal to the total
     * number of creatures that died this turn (across all players).
     */
    @HandlesEffect(DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect.class)
    void resolveDestroyTargetAndControllerLosesLifePerCreatureDeaths(GameData gameData, StackEntry entry,
                                                                     DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Find the controller before destruction
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (targetControllerId == null) {
            return;
        }

        // Destroy the target creature
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Count ALL creatures that died this turn (across all players, including tokens)
        int totalDeaths = 0;
        for (int count : gameData.creatureDeathCountThisTurn.values()) {
            totalDeaths += count;
        }

        if (totalDeaths > 0) {
            if (!gameQueryService.canPlayerLifeChange(gameData, targetControllerId)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        gameData.playerIdToName.get(targetControllerId) + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(targetControllerId);
                gameData.playerLifeTotals.put(targetControllerId, currentLife - totalDeaths);

                String playerName = gameData.playerIdToName.get(targetControllerId);
                String lifeLog = playerName + " loses " + totalDeaths + " life (" + entry.getCard().getName() + ").";
                gameBroadcastService.logAndBroadcast(gameData, lifeLog);
                log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, totalDeaths, entry.getCard().getName());
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves a {@link DestroyTargetLandAndDamageControllerEffect}, destroying the targeted
     * land and dealing noncombat damage to its controller. The damage is dealt regardless of
     * whether the destruction succeeds. Fizzles if the target is not a land.
     */
    @HandlesEffect(DestroyTargetLandAndDamageControllerEffect.class)
    void resolveDestroyTargetLandAndDamageController(GameData gameData, StackEntry entry,
                                                      DestroyTargetLandAndDamageControllerEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (!target.getCard().hasType(CardType.LAND)) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target type).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        // Find the controller of the targeted land before destruction
        UUID landControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (landControllerId == null) {
            return;
        }

        // Attempt to destroy the land
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Deal damage to the land's controller regardless of whether destruction succeeded
        dealNoncombatDamageToPlayer(gameData, landControllerId, effect.damage(),
                entry.getCard().getName(), entry.getCard().getColor());

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves a {@link DestroyTargetPermanentAndDamageControllerIfDestroyedEffect}, destroying the
     * targeted permanent and dealing noncombat damage to its controller only if the permanent was
     * actually put into a graveyard (e.g. not indestructible). Used by Werewolf Ransacker.
     */
    @HandlesEffect(DestroyTargetPermanentAndDamageControllerIfDestroyedEffect.class)
    void resolveDestroyTargetPermanentAndDamageControllerIfDestroyed(GameData gameData, StackEntry entry,
                                                                      DestroyTargetPermanentAndDamageControllerIfDestroyedEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Find the controller of the targeted permanent before destruction
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        // Attempt to destroy the permanent
        boolean destroyed = tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Deal damage only if the permanent was actually put into a graveyard
        if (destroyed) {
            dealNoncombatDamageToPlayer(gameData, targetControllerId, effect.damage(),
                    entry.getCard().getName(), entry.getCard().getColor());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves a {@link DestroyTargetPermanentAndGiveControllerPoisonCountersEffect}, destroying
     * the targeted permanent and giving its controller poison counters. The poison counters are
     * given regardless of whether the destruction succeeds (e.g. indestructible).
     */
    @HandlesEffect(DestroyTargetPermanentAndGiveControllerPoisonCountersEffect.class)
    void resolveDestroyTargetPermanentAndGiveControllerPoisonCounters(GameData gameData, StackEntry entry,
                                                                      DestroyTargetPermanentAndGiveControllerPoisonCountersEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Find the controller of the targeted permanent before destruction
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return;
        }

        // Attempt to destroy the target
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Give poison counters to the target's controller regardless of whether destruction succeeded
        if (gameQueryService.canPlayerGetPoisonCounters(gameData, controllerId)) {
            int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
            gameData.playerPoisonCounters.put(controllerId, currentPoison + effect.poisonCounters());

            String playerName = gameData.playerIdToName.get(controllerId);
            String poisonLog = playerName + " gets " + effect.poisonCounters() + " poison counter"
                    + (effect.poisonCounters() > 1 ? "s" : "") + " (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, poisonLog);
            log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName,
                    effect.poisonCounters(), entry.getCard().getName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves a {@link DestroyTargetPermanentAndControllerLosesLifeEffect}, destroying
     * the targeted permanent and causing its controller to lose life. The life loss occurs
     * regardless of whether the destruction succeeds (e.g. indestructible).
     */
    @HandlesEffect(DestroyTargetPermanentAndControllerLosesLifeEffect.class)
    void resolveDestroyTargetPermanentAndControllerLosesLife(GameData gameData, StackEntry entry,
                                                             DestroyTargetPermanentAndControllerLosesLifeEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Find the controller of the targeted permanent before destruction
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) {
            return;
        }

        // Attempt to destroy the target
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Target's controller loses life regardless of whether destruction succeeded
        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(controllerId) + "'s life total can't change.");
        } else {
            int currentLife = gameData.getLife(controllerId);
            gameData.playerLifeTotals.put(controllerId, currentLife - effect.lifeLoss());

            String playerName = gameData.playerIdToName.get(controllerId);
            String lifeLog = playerName + " loses " + effect.lifeLoss() + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lifeLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, effect.lifeLoss(), entry.getCard().getName());
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves a {@link SacrificeCreatureEffect}, forcing the targeted player to sacrifice
     * a creature. Delegates to {@link #performSacrificeCreatureForPlayer}.
     */
    @HandlesEffect(SacrificeCreatureEffect.class)
    void resolveSacrificeCreature(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        performSacrificeCreatureForPlayer(gameData, targetPlayerId);
    }

    /**
     * Resolves a {@link SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect},
     * forcing the targeted player to sacrifice a creature. The spell's controller gains
     * life equal to the sacrificed creature's toughness.
     */
    @HandlesEffect(SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect.class)
    void resolveSacrificeCreatureAndControllerGainsLifeEqualToToughness(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        List<UUID> creatureIds = collectCreatureIds(gameData, targetPlayerId, p -> true);

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures to sacrifice", gameData.id, playerName);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                int toughness = gameQueryService.getEffectiveToughness(gameData, creature);
                sacrificeAndLog(gameData, creature, targetPlayerId);
                lifeResolutionService.applyGainLife(gameData, controllerId, toughness, cardName);
            }
            return;
        }

        // Multiple creatures — prompt player to choose
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness(
                        targetPlayerId, controllerId, cardName));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature to sacrifice.");
    }

    /**
     * Resolves a {@link ControllerSacrificesCreatureEffect}, forcing the controller to sacrifice
     * a creature. Unlike {@link SacrificeCreatureEffect}, this does not target a player.
     */
    @HandlesEffect(ControllerSacrificesCreatureEffect.class)
    void resolveControllerSacrificesCreature(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        performSacrificeCreatureForPlayer(gameData, controllerId);
    }

    /**
     * Resolves a {@link SacrificeAttackingCreaturesEffect}, forcing the targeted player to
     * sacrifice a number of attacking creatures. The count depends on whether the controller
     * has metalcraft (3+ artifacts). If fewer attacking creatures exist than required, all
     * are sacrificed automatically; otherwise the player is prompted to choose.
     */
    @HandlesEffect(SacrificeAttackingCreaturesEffect.class)
    void resolveSacrificeAttackingCreatures(GameData gameData, StackEntry entry, SacrificeAttackingCreaturesEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        // Check metalcraft at resolution time (intervening-if)
        UUID controllerId = entry.getControllerId();
        int count = gameQueryService.isMetalcraftMet(gameData, controllerId)
                ? effect.metalcraftCount() : effect.baseCount();

        // Collect attacking creatures on target player's battlefield
        List<UUID> attackingCreatureIds = collectCreatureIds(gameData, targetPlayerId, Permanent::isAttacking);

        if (attackingCreatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no attacking creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no attacking creatures to sacrifice", gameData.id, playerName);
            return;
        }

        if (attackingCreatureIds.size() <= count) {
            // Auto-sacrifice all attacking creatures
            for (UUID creatureId : attackingCreatureIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
                if (creature != null) {
                    sacrificeAndLog(gameData, creature, targetPlayerId);
                }
            }
            return;
        }

        // More attacking creatures than required — prompt player to choose
        gameData.pendingSacrificeAttackingCreature = true;
        playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, attackingCreatureIds,
                count, "Choose " + count + " attacking creature" + (count > 1 ? "s" : "") + " to sacrifice.");
    }

    /**
     * Resolves an {@link EachOpponentSacrificesCreatureEffect}, forcing each opponent of
     * the controller to sacrifice a creature in turn order.
     */
    @HandlesEffect(EachOpponentSacrificesCreatureEffect.class)
    void resolveEachOpponentSacrificesCreature(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            performSacrificeCreatureForPlayer(gameData, playerId);
        }
    }

    /**
     * Resolves an {@link EachPlayerSacrificesPermanentsEffect}, forcing each player (in APNAP
     * order) to sacrifice a number of permanents matching the filter. Players with fewer
     * matching permanents than required sacrifice all of them automatically. Players with
     * more are prompted to choose.
     */
    @HandlesEffect(EachPlayerSacrificesPermanentsEffect.class)
    void resolveEachPlayerSacrificesPermanents(GameData gameData, StackEntry entry,
                                                EachPlayerSacrificesPermanentsEffect effect) {
        // Per CR 101.4 and Destructive Force ruling (2010-08-15): active player chooses first,
        // then each other player in turn order, then all chosen permanents are sacrificed at the
        // same time. Collect all IDs to sacrifice and defer actual sacrifice until all choices
        // are made.

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<Permanent> matching = battlefield.stream()
                    .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter()))
                    .toList();

            if (matching.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " has no matching permanents to sacrifice.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
                continue;
            }

            if (matching.size() <= effect.count()) {
                // No choice needed — mark all for simultaneous sacrifice
                matching.stream().map(Permanent::getId)
                        .forEach(gameData.pendingSimultaneousSacrificeIds::add);
            } else {
                // Player must choose — add to queue
                List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                gameData.pendingForcedSacrificeQueue.add(
                        new PendingForcedSacrifice(playerId, effect.count(), matchingIds));
            }
        }

        if (gameData.pendingForcedSacrificeQueue.isEmpty()) {
            // All players auto-resolved — sacrifice everything now
            performSimultaneousSacrifice(gameData);
        } else {
            // Some players need to choose — begin the first prompt
            beginNextForcedSacrificeFromQueue(gameData);
        }
    }

    /**
     * Resolves an {@link EachOpponentSacrificesPermanentsEffect}, forcing each opponent of
     * the controller to sacrifice a number of permanents matching the filter. Same APNAP
     * simultaneous-sacrifice logic as {@link EachPlayerSacrificesPermanentsEffect}, but
     * the controller is excluded.
     */
    @HandlesEffect(EachOpponentSacrificesPermanentsEffect.class)
    void resolveEachOpponentSacrificesPermanents(GameData gameData, StackEntry entry,
                                                  EachOpponentSacrificesPermanentsEffect effect) {
        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null || battlefield.isEmpty()) {
                continue;
            }

            List<Permanent> matching = battlefield.stream()
                    .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter()))
                    .toList();

            if (matching.isEmpty()) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " has no matching permanents to sacrifice.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
                continue;
            }

            if (matching.size() <= effect.count()) {
                matching.stream().map(Permanent::getId)
                        .forEach(gameData.pendingSimultaneousSacrificeIds::add);
            } else {
                List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
                gameData.pendingForcedSacrificeQueue.add(
                        new PendingForcedSacrifice(playerId, effect.count(), matchingIds));
            }
        }

        if (gameData.pendingForcedSacrificeQueue.isEmpty()) {
            performSimultaneousSacrifice(gameData);
        } else {
            beginNextForcedSacrificeFromQueue(gameData);
        }
    }

    /**
     * Resolves a {@link TargetPlayerSacrificesPermanentsEffect}, forcing the targeted player
     * to sacrifice a number of permanents matching the filter. The targeted player chooses
     * which permanents to sacrifice. If the player controls fewer matching permanents than
     * the count, they sacrifice all of them.
     */
    @HandlesEffect(TargetPlayerSacrificesPermanentsEffect.class)
    void resolveTargetPlayerSacrificesPermanents(GameData gameData, StackEntry entry,
                                                  TargetPlayerSacrificesPermanentsEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null || battlefield.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no permanents to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no permanents to sacrifice", gameData.id, playerName);
            return;
        }

        List<Permanent> matching = battlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter()))
                .toList();

        if (matching.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no matching permanents to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no matching permanents to sacrifice", gameData.id, playerName);
            return;
        }

        if (matching.size() <= effect.count()) {
            // Sacrifice all matching — no choice needed
            for (Permanent perm : matching) {
                sacrificeAndLog(gameData, perm, targetPlayerId);
            }
        } else {
            // More matching permanents than required — prompt player to choose
            List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
            gameData.pendingForcedSacrificeCount = effect.count();
            gameData.pendingForcedSacrificePlayerId = targetPlayerId;
            playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, matchingIds,
                    effect.count(), "Choose " + effect.count() + " permanent"
                            + (effect.count() > 1 ? "s" : "") + " to sacrifice.");
        }
    }

    /**
     * Sacrifices all permanents in {@link GameData#pendingSimultaneousSacrificeIds} at once,
     * then clears the list. Used to implement "all chosen permanents are sacrificed at the
     * same time" per CR 101.4.
     */
    void performSimultaneousSacrifice(GameData gameData) {
        List<UUID> ids = new ArrayList<>(gameData.pendingSimultaneousSacrificeIds);
        gameData.pendingSimultaneousSacrificeIds.clear();

        for (UUID permId : ids) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                sacrificeAndLog(gameData, perm, controllerId);
            }
        }
    }

    /**
     * Pops the next entry from the forced sacrifice queue and prompts that player to choose.
     * Does nothing if the queue is empty.
     */
    private void beginNextForcedSacrificeFromQueue(GameData gameData) {
        if (gameData.pendingForcedSacrificeQueue.isEmpty()) {
            return;
        }

        PendingForcedSacrifice next = gameData.pendingForcedSacrificeQueue.removeFirst();
        gameData.pendingForcedSacrificeCount = next.count();
        gameData.pendingForcedSacrificePlayerId = next.playerId();
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(), "Choose " + next.count() + " permanent"
                        + (next.count() > 1 ? "s" : "") + " to sacrifice.");
    }

    /**
     * Resolves a {@link DamageSourceControllerSacrificesPermanentsEffect}, forcing the damage
     * source's controller to sacrifice a number of permanents equal to the damage dealt.
     * The player chooses which permanents to sacrifice.
     */
    @HandlesEffect(DamageSourceControllerSacrificesPermanentsEffect.class)
    void resolveDamageSourceControllerSacrificesPermanents(GameData gameData, StackEntry entry,
                                                           DamageSourceControllerSacrificesPermanentsEffect effect) {
        UUID sacrificingPlayerId = effect.sacrificingPlayerId();
        int count = effect.count();

        if (sacrificingPlayerId == null || count <= 0 || !gameData.playerIds.contains(sacrificingPlayerId)) {
            return;
        }

        String playerName = gameData.playerIdToName.get(sacrificingPlayerId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(sacrificingPlayerId);
        if (battlefield == null || battlefield.isEmpty()) {
            String logEntry = playerName + " has no permanents to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no permanents to sacrifice", gameData.id, playerName);
            return;
        }

        List<UUID> permanentIds = battlefield.stream().map(Permanent::getId).toList();

        if (permanentIds.size() <= count) {
            // Sacrifice all — no choice needed
            for (Permanent perm : new ArrayList<>(battlefield)) {
                sacrificeAndLog(gameData, perm, sacrificingPlayerId);
            }
            return;
        }

        // More permanents than required — prompt player to choose
        gameData.pendingForcedSacrificeCount = count;
        gameData.pendingForcedSacrificePlayerId = sacrificingPlayerId;
        playerInputService.beginMultiPermanentChoice(gameData, sacrificingPlayerId, permanentIds,
                count, "Choose " + count + " permanent" + (count > 1 ? "s" : "") + " to sacrifice.");
    }

    /**
     * Forces a player to sacrifice a creature. If the player controls exactly one creature it
     * is sacrificed automatically; if multiple creatures exist the player is prompted to choose.
     * Does nothing if the player controls no creatures.
     *
     * @param gameData       the current game state
     * @param targetPlayerId the player who must sacrifice a creature
     */
    void performSacrificeCreatureForPlayer(GameData gameData, UUID targetPlayerId) {
        List<UUID> creatureIds = collectCreatureIds(gameData, targetPlayerId, p -> true);

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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

    /**
     * Resolves a {@link SacrificeOtherCreatureOrDamageEffect}. The controller must sacrifice
     * a creature other than the source. If no other creatures exist, the controller is dealt
     * noncombat damage instead.
     */
    @HandlesEffect(SacrificeOtherCreatureOrDamageEffect.class)
    void resolveSacrificeOtherCreatureOrDamage(GameData gameData, StackEntry entry, SacrificeOtherCreatureOrDamageEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        UUID sourceCardId = entry.getCard().getId();

        List<UUID> otherCreatureIds = collectCreatureIds(gameData, controllerId,
                p -> !p.getCard().getId().equals(sourceCardId));

        if (otherCreatureIds.isEmpty()) {
            // Can't sacrifice — deal damage to controller
            dealNoncombatDamageToPlayer(gameData, controllerId, effect.damage(), cardName, entry.getCard().getColor());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        if (otherCreatureIds.size() == 1) {
            // Only one other creature — sacrifice it automatically
            Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
            if (creature != null) {
                sacrificeAndLog(gameData, creature, controllerId);
            }
            return;
        }

        // Multiple other creatures — prompt player to choose
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                "Choose a creature other than " + cardName + " to sacrifice.");
    }

    /**
     * Resolves a {@link SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect}.
     * The controller must sacrifice a creature other than the source, then each opponent
     * loses life equal to the sacrificed creature's power. If no other creatures exist,
     * the source is tapped and the controller loses {@code lifeLoss} life.
     */
    @HandlesEffect(SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect.class)
    void resolveSacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLife(GameData gameData, StackEntry entry,
                                                                        SacrificeOtherCreatureOpponentsLoseLifeOrTapAndLoseLifeEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        UUID sourceCardId = entry.getCard().getId();

        List<UUID> otherCreatureIds = collectCreatureIds(gameData, controllerId,
                p -> !p.getCard().getId().equals(sourceCardId));

        if (otherCreatureIds.isEmpty()) {
            // Can't sacrifice — tap source and controller loses life
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (sourcePermanent != null) {
                sourcePermanent.tap();
                String tapLog = cardName + " is tapped.";
                gameBroadcastService.logAndBroadcast(gameData, tapLog);
                log.info("Game {} - {} is tapped (no creature to sacrifice)", gameData.id, cardName);
            }
            lifeResolutionService.applyLifeLoss(gameData, controllerId, effect.lifeLoss(), cardName);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        if (otherCreatureIds.size() == 1) {
            // Only one other creature — sacrifice it automatically
            Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
            if (creature != null) {
                int power = gameQueryService.getEffectivePower(gameData, creature);
                sacrificeAndLog(gameData, creature, controllerId);
                applyOpponentsLoseLife(gameData, controllerId, power, cardName);
            }
            return;
        }

        // Multiple other creatures — prompt player to choose
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreatureOpponentsLoseLife(controllerId, cardName));
        playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                "Choose a creature other than " + cardName + " to sacrifice.");
    }

    /**
     * Applies life loss to each opponent of the controller equal to the given amount.
     */
    public void applyOpponentsLoseLife(GameData gameData, UUID controllerId, int amount, String sourceName) {
        if (amount <= 0) return;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            lifeResolutionService.applyLifeLoss(gameData, playerId, amount, sourceName);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves a {@link DestroyCreatureBlockingThisEffect}, destroying the creature that is
     * blocking the source permanent. Fizzles if the target is no longer a creature.
     */
    @HandlesEffect(DestroyCreatureBlockingThisEffect.class)
    void resolveDestroyCreatureBlockingThis(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (!gameQueryService.isCreature(gameData, target)) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        tryDestroyAndLog(gameData, target, entry.getCard().getName());
    }

    /**
     * Resolves a {@link DestroyBlockedCreatureAndSelfEffect}, destroying both the blocked
     * attacker (target) and the source blocker.
     */
    @HandlesEffect(DestroyBlockedCreatureAndSelfEffect.class)
    void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (attacker != null) {
            tryDestroyAndLog(gameData, attacker, entry.getCard().getName());
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            tryDestroyAndLog(gameData, self, entry.getCard().getName());
        }
    }

    /**
     * Resolves a {@link DestroySourcePermanentEffect}, destroying the source permanent
     * identified by the stack entry's sourcePermanentId (e.g. Ice Cage destroying itself
     * when the enchanted creature becomes the target of a spell or ability).
     */
    @HandlesEffect(DestroySourcePermanentEffect.class)
    void resolveDestroySourcePermanent(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            tryDestroyAndLog(gameData, source, entry.getCard().getName());
        }
    }

    /**
     * Resolves a {@link DestroyTargetPermanentAndBoostSelfByManaValueEffect}, destroying the
     * targeted permanent and giving the source creature +X/+0 until end of turn, where X is
     * the destroyed permanent's mana value. The boost is applied regardless of whether
     * destruction succeeds (e.g. indestructible).
     */
    @HandlesEffect(DestroyTargetPermanentAndBoostSelfByManaValueEffect.class)
    void resolveDestroyTargetArtifactAndBoostSelfByManaValue(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();

        // Attempt to destroy the artifact
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Boost self by mana value regardless of destruction result
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null && manaValue > 0) {
            self.setPowerModifier(self.getPowerModifier() + manaValue);

            String boostLog = entry.getCard().getName() + " gets +" + manaValue + "/+0 until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, boostLog);
            log.info("Game {} - {} gets +{}/+0 from {}'s mana value", gameData.id, entry.getCard().getName(), manaValue, target.getCard().getName());
        }
    }

    /**
     * Resolves a {@link DestroyTargetPermanentAndGainLifeEqualToManaValueEffect}, destroying
     * the targeted permanent and granting the controller life equal to its mana value.
     * Life is gained regardless of whether destruction succeeds.
     */
    @HandlesEffect(DestroyTargetPermanentAndGainLifeEqualToManaValueEffect.class)
    void resolveDestroyTargetPermanentAndGainLifeEqualToManaValue(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();

        // Attempt to destroy the target
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Gain life equal to mana value regardless of destruction result
        if (manaValue > 0) {
            lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), manaValue,
                    "equal to " + target.getCard().getName() + "'s mana value");
        }
    }

    /**
     * Resolves a {@link DestroyTargetCreatureAndGainLifeEqualToToughnessEffect}, destroying
     * the targeted creature and granting the controller life equal to its toughness.
     * Life is gained regardless of whether destruction succeeds.
     */
    @HandlesEffect(DestroyTargetCreatureAndGainLifeEqualToToughnessEffect.class)
    void resolveDestroyTargetCreatureAndGainLifeEqualToToughness(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        // Attempt to destroy (life gain happens regardless)
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Gain life equal to toughness regardless of destruction result
        lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), toughness,
                "equal to " + target.getCard().getName() + "'s toughness");
    }

    private void createTokenForPlayer(GameData gameData, UUID controllerId,
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
                tokenCard.setPower(token.power());
                tokenCard.setToughness(token.toughness());
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
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} creates a {}/{} {} token for {}", gameData.id, sourceName,
                        token.power(), token.toughness(), token.tokenName(), playerName);

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            } else {
                String logEntry = playerName + " creates a " + colorName + token.tokenName() + " token.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} creates a {} token for {}", gameData.id, sourceName,
                        token.tokenName(), playerName);
            }
        }
    }

    @HandlesEffect(SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect.class)
    void resolveSacrificeSelfToDestroyCreature(GameData gameData, StackEntry entry) {
        UUID defenderId = entry.getTargetId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID controllerId = entry.getControllerId();

        if (defenderId == null || sourcePermanentId == null) {
            return;
        }

        // Check source creature is still on the battlefield
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles — source no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Collect valid creature targets from damaged player's battlefield
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<UUID> validCreatureIds = new ArrayList<>();
        if (defenderBattlefield != null) {
            for (Permanent perm : defenderBattlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    validCreatureIds.add(perm.getId());
                }
            }
        }

        if (validCreatureIds.isEmpty()) {
            String logEntry = entry.getCard().getName() + "'s ability resolves, but "
                    + gameData.playerIdToName.get(defenderId) + " has no creatures.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Present multi-permanent choice with max 1 to select destruction target
        gameData.pendingSacrificeSelfToDestroySourceId = sourcePermanentId;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validCreatureIds, 1,
                entry.getCard().getName() + "'s ability — Choose a creature "
                        + gameData.playerIdToName.get(defenderId) + " controls to destroy.");
    }

    /**
     * Resolves a {@link DestroyTargetPermanentAtEndStepEffect} by scheduling the target permanent
     * for destruction at the beginning of the next end step.
     */
    @HandlesEffect(DestroyTargetPermanentAtEndStepEffect.class)
    void resolveDestroyTargetPermanentAtEndStep(GameData gameData, StackEntry entry, DestroyTargetPermanentAtEndStepEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        gameData.pendingDestroyAtEndStep.add(target.getId());

        String logEntry = target.getCard().getName() + " will be destroyed at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} scheduled for destruction at end step", gameData.id, target.getCard().getName());
    }

    /**
     * Resolves a {@link SeparatePermanentsIntoPilesAndSacrificeEffect}. The controller
     * of the ability separates all permanents the target player controls into two piles,
     * then the target player chooses which pile to sacrifice.
     */
    @HandlesEffect(SeparatePermanentsIntoPilesAndSacrificeEffect.class)
    void resolveSeparatePermanentsIntoPilesAndSacrifice(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        UUID controllerId = entry.getControllerId();

        List<Permanent> permanents = gameData.playerBattlefields.get(targetPlayerId);
        if (permanents == null || permanents.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + " has no permanents to separate.");
            log.info("Game {} - {} has no permanents to separate", gameData.id, playerName);
            return;
        }

        List<UUID> allPermanentIds = permanents.stream().map(Permanent::getId).toList();

        // Store pile separation state
        gameData.pendingPileSeparation = true;
        gameData.pendingPileSeparationControllerId = controllerId;
        gameData.pendingPileSeparationTargetPlayerId = targetPlayerId;
        gameData.pendingPileSeparationAllPermanentIds.clear();
        gameData.pendingPileSeparationAllPermanentIds.addAll(allPermanentIds);
        gameData.pendingPileSeparationPile1Ids.clear();
        gameData.pendingPileSeparationPile2Ids.clear();

        // Prompt the controller to choose permanents for pile 1
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, allPermanentIds,
                allPermanentIds.size(),
                "Separate permanents into two piles. Select permanents for Pile 1 (unselected form Pile 2).");
    }

    /**
     * Completes step 1 of pile separation: the controller has assigned permanents to pile 1.
     * Now prompt the target player to choose which pile to sacrifice.
     */
    public void completePileSeparationStep1(GameData gameData, List<UUID> pile1Ids) {
        UUID targetPlayerId = gameData.pendingPileSeparationTargetPlayerId;

        gameData.pendingPileSeparationPile1Ids.addAll(pile1Ids);
        // Pile 2 is everything not in pile 1
        for (UUID permId : gameData.pendingPileSeparationAllPermanentIds) {
            if (!pile1Ids.contains(permId)) {
                gameData.pendingPileSeparationPile2Ids.add(permId);
            }
        }

        // Build pile descriptions for the prompt
        String pile1Desc = buildPileDescription(gameData, gameData.pendingPileSeparationPile1Ids);
        String pile2Desc = buildPileDescription(gameData, gameData.pendingPileSeparationPile2Ids);

        String controllerName = gameData.playerIdToName.get(gameData.pendingPileSeparationControllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " separates permanents into two piles. Pile 1: " + pile1Desc + ". Pile 2: " + pile2Desc + ".");

        // Prompt target player to choose which pile to sacrifice
        String prompt = "Choose a pile to sacrifice. Yes = Pile 1 (" + pile1Desc + "), No = Pile 2 (" + pile2Desc + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, targetPlayerId, List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Completes step 2 of pile separation: the target player has chosen which pile to sacrifice.
     *
     * @param accepted true = sacrifice pile 1, false = sacrifice pile 2
     */
    public void completePileSeparationStep2(GameData gameData, boolean accepted) {
        List<UUID> pileToSacrifice = accepted
                ? new ArrayList<>(gameData.pendingPileSeparationPile1Ids)
                : new ArrayList<>(gameData.pendingPileSeparationPile2Ids);
        String pileName = accepted ? "Pile 1" : "Pile 2";

        UUID targetPlayerId = gameData.pendingPileSeparationTargetPlayerId;
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        // Clean up pending state
        gameData.pendingPileSeparation = false;
        gameData.pendingPileSeparationControllerId = null;
        gameData.pendingPileSeparationTargetPlayerId = null;
        gameData.pendingPileSeparationAllPermanentIds.clear();
        gameData.pendingPileSeparationPile1Ids.clear();
        gameData.pendingPileSeparationPile2Ids.clear();

        String sacrificedDesc = buildPileDescription(gameData, pileToSacrifice);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " sacrifices " + pileName + ": " + sacrificedDesc + ".");

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
