package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
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
            gameBroadcastService.logAndBroadcast(gameData, sourceName + " resolves but no creatures are destroyed.");
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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} finds no nonland permanents with mana value {}", gameData.id, cardName, targetManaValue);
            return;
        }

        destroyBatch(gameData, toDestroy, cardName, false);
    }

    public void destroyBatch(GameData gameData, List<Permanent> toDestroy, String sourceName,
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

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    public boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        if (!permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated)) {
            return false;
        }
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), sourceName);
        return true;
    }

    public void sacrificeAndLog(GameData gameData, Permanent creature, UUID playerId) {
        permanentRemovalService.removePermanentToGraveyard(gameData, creature);
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
    }

    public void dealNoncombatDamageToPlayer(GameData gameData, UUID playerId, int baseDamage,
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

    void resolveForcedCostElseEffects(GameData gameData, StackEntry entry, ForcedCostOrElseEffect effect) {
        for (var elseEffect : effect.elseEffects()) {
            if (elseEffect instanceof TapPermanentsEffect tap && tap.scope() == TapUntapScope.SELF) {
                tapSourcePermanent(gameData, entry);
            } else if (elseEffect instanceof DealDamageToControllerEffect damage) {
                dealNoncombatDamageToPlayer(gameData, entry.getControllerId(), damage.damage(),
                        entry.getCard().getName(), entry.getCard().getColor());
                gameOutcomeService.checkWinCondition(gameData);
            } else {
                log.warn("Game {} - Unsupported ForcedCostOrElse fallback effect: {}",
                        gameData.id, elseEffect.getClass().getSimpleName());
            }
        }
    }

    private void tapSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent != null) {
            sourcePermanent.tap();
            String tapLog = sourcePermanent.getCard().getName() + " is tapped.";
            gameBroadcastService.logAndBroadcast(gameData, tapLog);
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
        gameBroadcastService.logAndBroadcast(gameData,
                controllerName + " separates permanents into two piles. Pile 1: " + pile1Desc + ". Pile 2: " + pile2Desc + ".");

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
