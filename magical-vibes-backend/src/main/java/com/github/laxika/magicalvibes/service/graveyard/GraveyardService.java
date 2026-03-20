package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ExileWithEggCountersInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GraveyardService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;
    // @Lazy to break indirect circular dependency:
    // GraveyardService → TriggerCollectionService → PermanentRemovalService → GraveyardService
    private TriggerCollectionService triggerCollectionService;

    public GraveyardService(GameQueryService gameQueryService,
                            GameBroadcastService gameBroadcastService,
                            ExileService exileService,
                            @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.exileService = exileService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Sets the TriggerCollectionService for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    // ===== Graveyard zone transitions =====

    public void resolveMillPlayer(GameData gameData, UUID targetPlayerId, int count) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsToMill = Math.min(count, deck.size());
        List<Card> milledCards = new ArrayList<>(deck.subList(0, cardsToMill));
        deck.subList(0, cardsToMill).clear();
        List<Card> cardsEnteredGraveyard = new ArrayList<>();
        for (Card card : milledCards) {
            boolean entered = addCardToGraveyard(gameData, targetPlayerId, card);
            if (entered) {
                cardsEnteredGraveyard.add(card);
            }
        }
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);

        // Fire creature-card-milled triggers (e.g. Undead Alchemist)
        for (Card card : cardsEnteredGraveyard) {
            if (card.hasType(CardType.CREATURE)) {
                triggerCollectionService.checkCreatureCardMilledTriggers(gameData, targetPlayerId, card);
            }
        }

        // Fire self-mill triggers (e.g. Gaea's Blessing — "When ~ is put into your graveyard
        // from your library, shuffle your graveyard into your library.")
        for (Card card : cardsEnteredGraveyard) {
            for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_MILLED)) {
                if (effect instanceof ShuffleGraveyardIntoLibraryEffect) {
                    List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
                    int graveyardCount = graveyard.size();
                    if (graveyardCount > 0) {
                        deck.addAll(graveyard);
                        graveyard.clear();
                        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                        String shuffleLog = card.getName() + " was milled — " + playerName
                                + " shuffles their graveyard (" + graveyardCount
                                + " card" + (graveyardCount != 1 ? "s" : "") + ") into their library.";
                        gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
                        log.info("Game {} - {} self-mill trigger: {} shuffles graveyard ({} cards) into library",
                                gameData.id, card.getName(), playerName, graveyardCount);
                    }
                }
            }
        }
    }

    /**
     * Adds a card to its owner's graveyard, or applies a replacement effect (e.g. shuffle into library).
     * Returns true if the card was actually put into the graveyard, false if a replacement effect was applied.
     * Callers should skip "dies" / graveyard triggers when this returns false (CR 614.6).
     */
    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card) {
        return addCardToGraveyard(gameData, ownerId, card, null);
    }

    public boolean addCardToGraveyard(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        // CR 614.7 — self-replacement effects apply first

        // "If [this] would die, instead exile it with N egg counters" (e.g. Darigaaz Reincarnated)
        // "Die" = move from battlefield to graveyard, so only applies when sourceZone is BATTLEFIELD.
        if (sourceZone == Zone.BATTLEFIELD && hasExileWithEggCountersReplacementEffect(card)) {
            ExileWithEggCountersInsteadOfDyingEffect eggEffect = getExileWithEggCountersReplacementEffect(card);
            exileService.exileCard(gameData, ownerId, card);
            gameData.exiledCardEggCounters.put(card.getId(), eggEffect.count());
            String exileLog = card.getName() + " is exiled with " + eggEffect.count() + " egg counters instead of dying.";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            log.info("Game {} - {} replacement effect: exiled with {} egg counters instead of dying",
                    gameData.id, card.getName(), eggEffect.count());
            return false;
        }

        if (hasShuffleIntoLibraryReplacementEffect(card)) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        // Leyline of the Void — if an opponent controls a permanent with
        // ExileOpponentCardsInsteadOfGraveyardEffect, exile the card instead
        if (opponentHasExileReplacementEffect(gameData, ownerId)) {
            exileService.exileCard(gameData, ownerId, card);
            String exileLog = card.getName() + " is exiled instead of being put into a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, exileLog);
            log.info("Game {} - {} replacement effect: exiled instead of graveyard", gameData.id, card.getName());
            return false;
        }

        gameData.playerGraveyards.get(ownerId).add(card);
        updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone);
        updateFromAnywhereThisTurnTracking(gameData, ownerId, card);
        return true;
    }

    // ===== Regeneration =====

    public boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.isCantRegenerateThisTurn()) {
            return false;
        }
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            perm.tap();
            triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
            perm.setAttacking(false);
            perm.setBlocking(false);
            perm.getBlockingTargets().clear();
            // CR 701.15a — regeneration removes all damage marked on the permanent
            perm.setMarkedDamage(0);

            String logEntry = perm.getCard().getName() + " regenerates.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Damage tracking =====

    public void recordCreatureDamagedByPermanent(GameData gameData, UUID sourcePermanentId, Permanent damagedCreature, int damage) {
        if (sourcePermanentId == null || damagedCreature == null || damage <= 0) {
            return;
        }
        if (!gameQueryService.isCreature(gameData, damagedCreature)) {
            return;
        }

        gameData.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(sourcePermanentId, ignored -> ConcurrentHashMap.newKeySet())
                .add(damagedCreature.getCard().getId());
    }

    // ===== Private helpers =====

    private boolean hasExileWithEggCountersReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ExileWithEggCountersInsteadOfDyingEffect);
    }

    private ExileWithEggCountersInsteadOfDyingEffect getExileWithEggCountersReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ExileWithEggCountersInsteadOfDyingEffect)
                .map(e -> (ExileWithEggCountersInsteadOfDyingEffect) e)
                .findFirst()
                .orElseThrow();
    }

    private boolean hasShuffleIntoLibraryReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ShuffleIntoLibraryReplacementEffect);
    }

    private boolean opponentHasExileReplacementEffect(GameData gameData, UUID ownerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(ownerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ExileOpponentCardsInsteadOfGraveyardEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateFromAnywhereThisTurnTracking(GameData gameData, UUID ownerId, Card card) {
        if (!card.isToken()) {
            gameData.cardsPutIntoGraveyardFromAnywhereThisTurn
                    .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(card.getId());
        }
    }

    private void updateThisTurnBattlefieldToGraveyardTracking(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        if (sourceZone == Zone.BATTLEFIELD
                && !card.isToken()
                && card.hasType(CardType.CREATURE)) {
            tracked.add(card.getId());
            triggerDamagedCreatureDiesAbilities(gameData, card);
        } else {
            tracked.remove(card.getId());
        }
    }

    private void triggerDamagedCreatureDiesAbilities(GameData gameData, Card dyingCreatureCard) {
        if (dyingCreatureCard == null) {
            return;
        }

        UUID dyingCreatureCardId = dyingCreatureCard.getId();

        for (Map.Entry<UUID, Set<UUID>> entry : gameData.creatureCardsDamagedThisTurnBySourcePermanent.entrySet()) {
            UUID sourcePermanentId = entry.getKey();
            Set<UUID> damagedCreatureIds = entry.getValue();
            if (!damagedCreatureIds.contains(dyingCreatureCardId)) {
                continue;
            }

            Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source == null) {
                continue;
            }

            UUID controllerId = findPermanentController(gameData, sourcePermanentId);
            if (controllerId == null) {
                continue;
            }

            List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES);
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            for (CardEffect effect : effects) {
                // Convert GainLifeEqualToToughnessEffect to a concrete GainLifeEffect
                // using the dying creature's toughness (last known information)
                CardEffect resolvedEffect = effect;
                if (effect instanceof GainLifeEqualToToughnessEffect) {
                    resolvedEffect = new GainLifeEffect(dyingCreatureCard.getToughness());
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolvedEffect)),
                        null,
                        sourcePermanentId
                ));
                String triggerLog = source.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers (damaged creature died this turn)", gameData.id, source.getCard().getName());
            }
        }

        for (Set<UUID> damagedCreatureIds : gameData.creatureCardsDamagedThisTurnBySourcePermanent.values()) {
            damagedCreatureIds.remove(dyingCreatureCardId);
        }
    }

    private UUID findPermanentController(GameData gameData, UUID permanentId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(permanentId)) {
                    return playerId;
                }
            }
        }
        return null;
    }
}
