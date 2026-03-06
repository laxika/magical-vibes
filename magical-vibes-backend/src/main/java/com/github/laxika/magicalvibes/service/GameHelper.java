package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GameHelper {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    // @Lazy to break indirect circular dependency:
    // GameHelper → TriggerCollectionService → PermanentRemovalService → GameHelper
    private TriggerCollectionService triggerCollectionService;

    public GameHelper(GameQueryService gameQueryService,
                      GameBroadcastService gameBroadcastService,
                      @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Sets the TriggerCollectionService for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setTriggerCollectionService(TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    // ===== General utility =====

    public void setImprintedCardOnPermanent(GameData gameData, UUID sourcePermanentId, Card card) {
        Permanent perm = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (perm != null) {
            perm.getCard().setImprintedCard(card);
        }
    }

    // ===== Lifecycle methods =====

    public void resolveMillPlayer(GameData gameData, UUID targetPlayerId, int count) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        int cardsToMill = Math.min(count, deck.size());
        List<Card> milledCards = new ArrayList<>(deck.subList(0, cardsToMill));
        deck.subList(0, cardsToMill).clear();
        for (Card card : milledCards) {
            addCardToGraveyard(gameData, targetPlayerId, card);
        }
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + " mills " + cardsToMill + " card" + (cardsToMill != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} mills {} cards", gameData.id, playerName, cardsToMill);
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
        if (card.isShufflesIntoLibraryFromGraveyard()) {
            List<Card> deck = gameData.playerDecks.get(ownerId);
            deck.add(card);
            Collections.shuffle(deck);
            String shuffleLog = card.getName() + " is revealed and shuffled into its owner's library instead.";
            gameBroadcastService.logAndBroadcast(gameData, shuffleLog);
            log.info("Game {} - {} replacement effect: shuffled into library instead of graveyard", gameData.id, card.getName());
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, null);
            return false;
        } else {
            gameData.playerGraveyards.get(ownerId).add(card);
            updateThisTurnBattlefieldToGraveyardTracking(gameData, ownerId, card, sourceZone);
            return true;
        }
    }

    private void updateThisTurnBattlefieldToGraveyardTracking(GameData gameData, UUID ownerId, Card card, Zone sourceZone) {
        Set<UUID> tracked = gameData.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn
                .computeIfAbsent(ownerId, ignored -> ConcurrentHashMap.newKeySet());
        if (sourceZone == Zone.BATTLEFIELD
                && !card.isToken()
                && (card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE))) {
            tracked.add(card.getId());
            triggerDamagedCreatureDiesAbilities(gameData, card.getId());
        } else {
            tracked.remove(card.getId());
        }
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

    private void triggerDamagedCreatureDiesAbilities(GameData gameData, UUID dyingCreatureCardId) {
        if (dyingCreatureCardId == null) {
            return;
        }

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
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
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

    // ===== End-of-turn cleanup =====

    void resetEndOfTurnModifiers(GameData gameData) {
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0 || !p.getGrantedKeywords().isEmpty()
                    || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0 || p.isCantBeBlocked()
                    || p.isAnimatedUntilEndOfTurn() || p.isCantRegenerateThisTurn()
                    || p.isExileInsteadOfDieThisTurn() || !p.getGrantedCardTypes().isEmpty()) {
                p.resetModifiers();
                p.setDamagePreventionShield(0);
                p.setRegenerationShield(0);
            }
        });

        gameData.playerDamagePreventionShields.clear();
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.preventDamageFromColors.clear();
        gameData.combatDamageRedirectTarget = null;
        gameData.playerColorDamagePreventionCount.clear();
        gameData.playerSourceDamagePreventionIds.clear();
        gameData.permanentsPreventedFromDealingDamage.clear();
        gameData.drawReplacementTargetToController.clear();
    }

    void drainManaPools(GameData gameData) {
        // Check if any permanent on the battlefield prevents mana drain (e.g. Upwelling)
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PreventManaDrainEffect.class::isInstance)) {
                    return;
                }
            }
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
            }
        }
    }

    boolean hasNoMaximumHandSize(GameData gameData, UUID playerId) {
        if (gameData.playersWithNoMaximumHandSize.contains(playerId)) {
            return true;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    // ===== Mana =====

    public void payGenericMana(ManaPool pool, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ManaColor highestColor = null;
            int highestCount = 0;
            for (ManaColor color : ManaColor.values()) {
                int count = pool.get(color);
                if (count > highestCount) {
                    highestCount = count;
                    highestColor = color;
                }
            }
            if (highestColor != null) {
                pool.remove(highestColor);
                remaining--;
            } else {
                break;
            }
        }
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

            String logEntry = perm.getCard().getName() + " regenerates.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} regenerates", gameData.id, perm.getCard().getName());
            return true;
        }
        return false;
    }

    // ===== Exile (used by CombatService) =====

    public void resolveExileTopCardsRepeatOnDuplicate(GameData gameData, Card sourceCard, UUID targetPlayerId, ExileTopCardsRepeatOnDuplicateEffect effect) {
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> exiled = gameData.playerExiledCards.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String creatureName = sourceCard.getName();

        String triggerLog = creatureName + "'s ability triggers — " + playerName + " exiles cards from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);

        boolean repeat = true;
        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty. No cards to exile.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                break;
            }

            int cardsToExile = Math.min(effect.count(), deck.size());
            List<Card> exiledThisRound = new ArrayList<>();
            for (int i = 0; i < cardsToExile; i++) {
                Card card = deck.removeFirst();
                exiled.add(card);
                exiledThisRound.add(card);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(playerName).append(" exiles ");
            for (int i = 0; i < exiledThisRound.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(exiledThisRound.get(i).getName());
            }
            sb.append(".");
            gameBroadcastService.logAndBroadcast(gameData, sb.toString());

            Set<String> seen = new HashSet<>();
            for (Card card : exiledThisRound) {
                if (!seen.add(card.getName())) {
                    repeat = true;
                    break;
                }
            }

            if (repeat) {
                String repeatLog = "Two or more exiled cards share the same name — repeating the process.";
                gameBroadcastService.logAndBroadcast(gameData, repeatLog);
            }
        }

        log.info("Game {} - {} exile trigger resolved for {}", gameData.id, creatureName, playerName);
    }
}
