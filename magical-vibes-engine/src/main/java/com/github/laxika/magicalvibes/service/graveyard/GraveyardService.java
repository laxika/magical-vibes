package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.RegeneratesIfWouldBeDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileWithEggCountersInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
                        clearGraveyard(gameData, targetPlayerId);
                        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
                        String shuffleLog = card.getName() + " was milled — " + playerName
                                + " shuffles their graveyard (" + graveyardCount
                                + " card" + (graveyardCount != 1 ? "s" : "") + ") into their library.";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(shuffleLog));
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} replacement effect: exiled with {} egg counters instead of dying",
                    gameData.id, card.getName(), eggEffect.count());
            return false;
        }

        // "If [this creature] would die, put it on top of its owner's library instead" (Gravebane Zombie)
        // "Die" = move from battlefield to graveyard, so only applies when sourceZone is BATTLEFIELD.
        if (sourceZone == Zone.BATTLEFIELD && hasPutOnTopOfLibraryInsteadOfDyingEffect(card)) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(0, card);
            String topLog = card.getName() + " is put on top of its owner's library instead of dying.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(topLog));
            log.info("Game {} - {} replacement effect: put on top of library instead of dying", gameData.id, card.getName());
            return false;
        }

        if (hasShuffleIntoLibraryReplacementEffect(card)) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(shuffleLog));
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        // Wheel of Sun and Moon — if the graveyard's owner is enchanted by a player aura with this
        // replacement, cards headed to their graveyard from anywhere are revealed and put on the
        // bottom of their library instead. Tokens are not cards, so they still hit the graveyard.
        if (!card.isToken() && enchantedPlayerHasBottomOfLibraryReplacement(gameData, ownerId)) {
            gameData.playerDecks.get(ownerId).add(card);
            String bottomLog = card.getName() + " is revealed and put on the bottom of its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(bottomLog));
            log.info("Game {} - {} replacement effect: put on bottom of library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        }

        // Per-card "if that spell would be put into a graveyard, exile it instead" replacement
        // (e.g. a spell cast via Nita, Forum Conciliator). Tracked for the specific card until cleanup.
        if (gameData.exileInsteadOfGraveyard.remove(card.getId())) {
            exileService.exileCard(gameData, ownerId, card);
            String exileLog = card.getName() + " is exiled instead of being put into a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard (cast permission)",
                    gameData.id, card.getName());
            return false;
        }

        // Leyline of the Void — if an opponent controls a permanent with
        // ExileOpponentCardsInsteadOfGraveyardEffect, exile the card instead
        if (opponentHasExileReplacementEffect(gameData, ownerId)) {
            exileService.exileCard(gameData, ownerId, card);
            String exileLog = card.getName() + " is exiled instead of being put into a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard", gameData.id, card.getName());
            return false;
        }

        // Forbidden Crypt — if the graveyard's owner controls a permanent with
        // ExileOwnCardsInsteadOfGraveyardEffect, exile the card instead
        if (ownerHasExileOwnGraveyardReplacementEffect(gameData, ownerId)) {
            exileService.exileCard(gameData, ownerId, card);
            String exileLog = card.getName() + " is exiled instead of being put into a graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(exileLog));
            log.info("Game {} - {} replacement effect: exiled instead of graveyard (own)", gameData.id, card.getName());
            return false;
        }

        gameData.playerGraveyards.get(ownerId).add(card);
        updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone);
        updateFromAnywhereThisTurnTracking(gameData, ownerId, card);
        collectPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        if (sourceZone == Zone.BATTLEFIELD) {
            collectPutIntoGraveyardFromBattlefieldTriggers(gameData, ownerId, card);
        }
        if (!card.isToken() && card.hasType(CardType.LAND)) {
            triggerCollectionService.checkLandPutIntoGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        }
        triggerCollectionService.checkBlackCardPutIntoOpponentGraveyardFromAnywhereTriggers(gameData, ownerId, card);
        return true;
    }

    /**
     * Exiles up to {@code count} cards from the given player's graveyard (from the front of the
     * list) and returns the number actually exiled. Used by prevention effects that exile a card
     * from the graveyard for each 1 damage prevented (Immortal Coil). Exiling from one's own
     * graveyard is a shortcut choice, so the front cards are taken deterministically.
     */
    public int exileCardsFromGraveyard(GameData gameData, UUID playerId, int count) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null || graveyard.isEmpty() || count <= 0) {
            return 0;
        }
        int toExile = Math.min(count, graveyard.size());
        List<Card> exiled = new ArrayList<>(graveyard.subList(0, toExile));
        graveyard.subList(0, toExile).clear();
        for (Card card : exiled) {
            exileService.exileCard(gameData, playerId, card);
        }
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " exiles " + toExile + " card" + (toExile != 1 ? "s" : "")
                + " from their graveyard.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} exiles {} cards from graveyard", gameData.id, playerName, toExile);
        return toExile;
    }

    /**
     * Fires "when this card is put into a graveyard from anywhere" triggered abilities
     * (EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, e.g. Purity). The card has already
     * entered the graveyard; the trigger goes on the stack under its owner's control.
     */
    private void collectPutIntoGraveyardFromAnywhereTriggers(GameData gameData, UUID ownerId, Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    ownerId,
                    card.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    (UUID) null
            ));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers (put into graveyard from anywhere)", gameData.id, card.getName());
        }
    }

    /**
     * Fires "when this card is put into a graveyard from the battlefield" triggered abilities
     * (EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, e.g. Spreading Algae). Only called when
     * the source zone is the battlefield. The card has already entered the graveyard; the trigger goes
     * on the stack under its owner's control.
     */
    private void collectPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID ownerId, Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    ownerId,
                    card.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    (UUID) null
            ));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers (put into graveyard from battlefield)", gameData.id, card.getName());
        }
    }


    public boolean tryRegenerate(GameData gameData, Permanent perm) {
        if (perm.isCantRegenerateThisTurn()) {
            return false;
        }
        // Always-on intrinsic regeneration ("If this creature would be destroyed, regenerate it")
        // — regenerates every time without consuming a shield.
        boolean intrinsicRegen = perm.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(RegeneratesIfWouldBeDestroyedEffect.class::isInstance);
        if (intrinsicRegen) {
            performRegeneration(gameData, perm);
            return true;
        }
        if (perm.getRegenerationShield() > 0) {
            perm.setRegenerationShield(perm.getRegenerationShield() - 1);
            performRegeneration(gameData, perm);
            return true;
        }
        return false;
    }

    private void performRegeneration(GameData gameData, Permanent perm) {
        perm.tap();
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, perm);
        perm.setAttacking(false);
        perm.setBlocking(false);
        perm.getBlockingTargets().clear();
        // CR 701.15a — regeneration removes all damage marked on the permanent
        perm.setMarkedDamage(0);

        String logEntry = perm.getCard().getName() + " regenerates.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
    }


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

    private boolean hasPutOnTopOfLibraryInsteadOfDyingEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof PutOnTopOfLibraryInsteadOfDyingEffect);
    }

    private boolean hasShuffleIntoLibraryReplacementEffect(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ShuffleIntoLibraryReplacementEffect);
    }

    private boolean enchantedPlayerHasBottomOfLibraryReplacement(GameData gameData, UUID ownerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.isAttached() && ownerId.equals(p.getAttachedTo())
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(RevealAndPutOnBottomOfLibraryInsteadOfGraveyardEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
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

    private boolean ownerHasExileOwnGraveyardReplacementEffect(GameData gameData, UUID ownerId) {
        List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
        if (bf == null) {
            return false;
        }
        for (Permanent p : bf) {
            if (p.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(ExileOwnCardsInsteadOfGraveyardEffect.class::isInstance)) {
                return true;
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
        // Tracks all non-token cards (any type) put into the graveyard from the battlefield this turn.
        Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        if (sourceZone == Zone.BATTLEFIELD && !card.isToken()) {
            allTracked.add(card.getId());
            if (card.hasType(CardType.CREATURE)) {
                tracked.add(card.getId());
                triggerDamagedCreatureDiesAbilities(gameData, card);
            } else {
                tracked.remove(card.getId());
            }
        } else {
            tracked.remove(card.getId());
            allTracked.remove(card.getId());
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
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


    /**
     * Begins a batch of graveyard removals that should produce a single
     * "one or more cards leave your graveyard" trigger event.
     */
    public void beginGraveyardLeaveBatch(GameData gameData) {
        gameData.graveyardLeaveNotificationDepth++;
    }

    /**
     * Ends a graveyard-leave batch and fires any deferred triggers.
     */
    public void endGraveyardLeaveBatch(GameData gameData) {
        if (gameData.graveyardLeaveNotificationDepth <= 0) {
            return;
        }
        gameData.graveyardLeaveNotificationDepth--;
        if (gameData.graveyardLeaveNotificationDepth == 0) {
            for (UUID ownerId : gameData.graveyardLeaveNotificationPendingOwners) {
                triggerCollectionService.checkControllerCardsLeaveGraveyardTriggers(gameData, ownerId);
            }
            gameData.graveyardLeaveNotificationPendingOwners.clear();
        }
    }

    /**
     * Notifies that one or more cards left the given player's graveyard.
     * When inside a batch ({@link #beginGraveyardLeaveBatch}), defers until the batch ends.
     */
    public void notifyCardsLeftGraveyard(GameData gameData, UUID ownerId) {
        // Record that one or more cards left this player's graveyard this turn (regardless of
        // batching), for "if one or more cards left your graveyard this turn" effects.
        gameData.playersWhoseCardsLeftGraveyardThisTurn.add(ownerId);
        if (gameData.graveyardLeaveNotificationDepth > 0) {
            gameData.graveyardLeaveNotificationPendingOwners.add(ownerId);
            return;
        }
        triggerCollectionService.checkControllerCardsLeaveGraveyardTriggers(gameData, ownerId);
    }

    /**
     * Clears a player's graveyard and fires a single leave-graveyard trigger if it was non-empty.
     * The cards must already have been moved to their destination zone (exile, etc.) by the caller —
     * this only empties the graveyard list and fires the trigger.
     */
    public void clearGraveyard(GameData gameData, UUID ownerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }
        graveyard.clear();
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.get(ownerId);
        if (tracked != null) {
            tracked.clear();
        }
        Set<UUID> allTracked = gameData.cardsPutIntoGraveyardFromBattlefieldThisTurn.get(ownerId);
        if (allTracked != null) {
            allTracked.clear();
        }
        notifyCardsLeftGraveyard(gameData, ownerId);
    }
}
