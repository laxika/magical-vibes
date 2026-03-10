package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetPlayerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.effect.DrainLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesXLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerCreatureControlledEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerPoisonCountersOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEnchantedPermanentControllerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToTargetToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachSubtypeOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerMatchingPermanentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LifeResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @HandlesEffect(GainLifeEffect.class)
    private void resolveGainLife(GameData gameData, StackEntry entry, GainLifeEffect effect) {
        applyGainLife(gameData, entry.getControllerId(), effect.amount());
    }

    @HandlesEffect(PayXManaGainXLifeEffect.class)
    private void resolvePayXManaGainXLife(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Re-entry after player chose X value
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;
            String cardName = entry.getCard().getName();
            String playerName = gameData.playerIdToName.get(controllerId);

            if (chosenValue == 0) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " chooses X=0 for " + cardName + "'s ability.");
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            new ManaCost("{0}").pay(pool, chosenValue);

            if (gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
                gameData.playerLifeTotals.put(controllerId, currentLife + chosenValue);

                String logEntry = playerName + " pays {" + chosenValue + "} and gains " + chosenValue + " life (" + cardName + ").";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} pays {} mana and gains {} life from {}",
                        gameData.id, playerName, chosenValue, chosenValue, cardName);
            } else {
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            }
            return;
        }

        // First call: prompt for X value
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int maxX = pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
        if (maxX <= 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " has no mana to pay for " + entry.getCard().getName() + "'s ability.");
            log.info("Game {} - {} has no mana for {}'s pay-X ability", gameData.id,
                    gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }
        String prompt = "Pay {X} for " + entry.getCard().getName() + "? You gain X life.";
        playerInputService.beginXValueChoice(gameData, controllerId, maxX, prompt, entry.getCard().getName());
    }

    public void applyGainLife(GameData gameData, UUID controllerId, int amount) {
        applyGainLife(gameData, controllerId, amount, null);
    }

    public void applyGainLife(GameData gameData, UUID controllerId, int amount, String source) {
        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }
        Integer currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + amount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = source != null
                ? playerName + " gains " + amount + " life from " + source + "."
                : playerName + " gains " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gains {} life", gameData.id, playerName, amount);

        triggerCollectionService.checkLifeGainTriggers(gameData, controllerId, amount);
    }

    @HandlesEffect(GainLifePerCardsInHandEffect.class)
    private void resolveGainLifePerCardsInHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        int cardCount = hand != null ? hand.size() : 0;
        if (cardCount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " gains no life (no cards in hand).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        applyGainLife(gameData, controllerId, cardCount);
    }

    @HandlesEffect(GainLifePerCreatureOnBattlefieldEffect.class)
    private void resolveGainLifePerCreatureOnBattlefield(GameData gameData, StackEntry entry) {
        int[] creatureCountHolder = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)) {
                creatureCountHolder[0]++;
            }
        });
        int creatureCount = creatureCountHolder[0];
        if (creatureCount == 0) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            String logEntry = playerName + " gains no life (no creatures on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        applyGainLife(gameData, entry.getControllerId(), creatureCount);
    }

    @HandlesEffect(GainLifePerMatchingPermanentOnBattlefieldEffect.class)
    private void resolveGainLifePerMatchingPermanentOnBattlefield(GameData gameData, StackEntry entry,
                                                                   GainLifePerMatchingPermanentOnBattlefieldEffect effect) {
        int totalCount = 0;
        for (PermanentPredicate predicate : effect.predicates()) {
            int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                if (gameQueryService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                    count[0]++;
                }
            });
            totalCount += count[0];
        }
        if (totalCount == 0) {
            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            String logEntry = playerName + " gains no life (no matching permanents on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        applyGainLife(gameData, entry.getControllerId(), totalCount);
    }

    @HandlesEffect(GainLifeForEachSubtypeOnBattlefieldEffect.class)
    private void resolveGainLifeForEachSubtypeOnBattlefield(GameData gameData, StackEntry entry,
                                                             GainLifeForEachSubtypeOnBattlefieldEffect effect) {
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getSubtypes().contains(effect.subtype())) {
                count[0]++;
            }
        });
        if (count[0] == 0) {
            return;
        }
        applyGainLife(gameData, entry.getControllerId(), count[0]);
    }

    @HandlesEffect(GainLifePerControlledCreatureEffect.class)
    private void resolveGainLifePerControlledCreature(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int creatureCount = 0;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureCount++;
                }
            }
        }
        if (creatureCount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " gains no life (no creatures on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        applyGainLife(gameData, controllerId, creatureCount);
    }

    @HandlesEffect(GainLifePerGraveyardCardEffect.class)
    private void resolveGainLifePerGraveyardCard(GameData gameData, StackEntry entry) {
        applyGainLifePerGraveyardCard(gameData, entry.getControllerId());
    }

    private void applyGainLifePerGraveyardCard(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        int amount = graveyard != null ? graveyard.size() : 0;
        if (amount == 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            String logEntry = playerName + " has no cards in their graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no graveyard cards for life gain", gameData.id, playerName);
            return;
        }
        applyGainLife(gameData, controllerId, amount);
    }

    @HandlesEffect(GainLifeEqualToTargetToughnessEffect.class)
    private void resolveGainLifeEqualToTargetToughness(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            gameData.playerIdToName.get(playerId) + "'s life total can't change.");
                    break;
                }
                int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                gameData.playerLifeTotals.put(playerId, currentLife + toughness);

                String logEntry = gameData.playerIdToName.get(playerId) + " gains " + toughness + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                log.info("Game {} - {} gains {} life (equal to {}'s toughness)",
                        gameData.id, gameData.playerIdToName.get(playerId), toughness, target.getCard().getName());
                break;
            }
        }
    }

    @HandlesEffect(GainLifeEqualToChargeCountersOnSourceEffect.class)
    private void resolveGainLifeEqualToChargeCounters(GameData gameData, StackEntry entry) {
        int count = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (count <= 0) {
            String logEntry = playerName + " gains 0 life from " + entry.getCard().getName() + " (no charge counters).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gains 0 life from {} (no charge counters)", gameData.id, playerName, entry.getCard().getName());
            return;
        }

        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }

        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + count);

        String logEntry = playerName + " gains " + count + " life from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains {} life from {}", gameData.id, playerName, count, entry.getCard().getName());
    }

    @HandlesEffect(DoubleTargetPlayerLifeEffect.class)
    private void resolveDoubleTargetPlayerLife(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();

        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }

        int currentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
        int newLife = currentLife * 2;
        gameData.playerLifeTotals.put(targetPlayerId, newLife);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = playerName + "'s life total is doubled from " + currentLife + " to " + newLife + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {}'s life doubled from {} to {}", gameData.id, playerName, currentLife, newLife);
    }

    @HandlesEffect(ExchangeTargetPlayersLifeTotalsEffect.class)
    private void resolveExchangeTargetPlayersLifeTotals(GameData gameData, StackEntry entry) {
        List<UUID> targets = entry.getTargetPermanentIds();
        if (targets.size() != 2) return;

        UUID playerA = targets.get(0);
        UUID playerB = targets.get(1);

        // CR 118.7: If either player's life total can't change, the exchange doesn't occur
        if (!gameQueryService.canPlayerLifeChange(gameData, playerA)) {
            String playerName = gameData.playerIdToName.get(playerA);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change. Exchange doesn't occur.");
            return;
        }
        if (!gameQueryService.canPlayerLifeChange(gameData, playerB)) {
            String playerName = gameData.playerIdToName.get(playerB);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change. Exchange doesn't occur.");
            return;
        }

        int lifeA = gameData.playerLifeTotals.getOrDefault(playerA, 20);
        int lifeB = gameData.playerLifeTotals.getOrDefault(playerB, 20);

        if (lifeA == lifeB) {
            String nameA = gameData.playerIdToName.get(playerA);
            String nameB = gameData.playerIdToName.get(playerB);
            String logEntry = nameA + " and " + nameB + " exchange life totals (both at " + lifeA + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        String nameA = gameData.playerIdToName.get(playerA);
        String nameB = gameData.playerIdToName.get(playerB);
        String logEntry = nameA + " and " + nameB + " exchange life totals (" + nameA + ": " + lifeA + " -> " + lifeB + ", " + nameB + ": " + lifeB + " -> " + lifeA + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        // Per Scryfall ruling: "each player gains or loses the amount of life necessary
        // to equal the other player's previous life total." Apply as gain/loss so triggers fire.
        String cardName = entry.getCard().getName();
        gameData.playerLifeTotals.put(playerA, lifeB);
        gameData.playerLifeTotals.put(playerB, lifeA);

        // Fire life loss/gain triggers for the player(s) who lost or gained life
        if (lifeB < lifeA) {
            triggerCollectionService.checkLifeLossTriggers(gameData, playerA, lifeA - lifeB);
            triggerCollectionService.checkLifeGainTriggers(gameData, playerB, lifeA - lifeB);
        }
        if (lifeA < lifeB) {
            triggerCollectionService.checkLifeLossTriggers(gameData, playerB, lifeB - lifeA);
            triggerCollectionService.checkLifeGainTriggers(gameData, playerA, lifeB - lifeA);
        }

        log.info("Game {} - {} and {} exchange life totals ({} -> {}, {} -> {})",
                gameData.id, nameA, nameB, lifeA, lifeB, lifeB, lifeA);
    }

    @HandlesEffect(EnchantedCreatureControllerLosesLifeEffect.class)
    private void resolveEnchantedCreatureControllerLosesLife(GameData gameData, StackEntry entry, EnchantedCreatureControllerLosesLifeEffect effect) {
        UUID playerId = effect.affectedPlayerId();
        if (playerId == null) return;

        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }

        int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
        gameData.playerLifeTotals.put(playerId, currentLife - effect.amount());

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + effect.amount() + " life (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, effect.amount(), entry.getCard().getName());
    }

    @HandlesEffect(LoseLifeEffect.class)
    private void resolveLoseLife(GameData gameData, StackEntry entry, LoseLifeEffect effect) {
        applyLifeLoss(gameData, entry.getControllerId(), effect.amount(), entry.getCard().getName());
    }

    @HandlesEffect(TargetSpellControllerLosesLifeEffect.class)
    private void resolveTargetSpellControllerLosesLife(GameData gameData, StackEntry entry, TargetSpellControllerLosesLifeEffect effect) {
        UUID targetCardId = entry.getTargetPermanentId();
        if (targetCardId == null) return;

        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                applyLifeLoss(gameData, se.getControllerId(), effect.amount(), entry.getCard().getName());
                return;
            }
        }
        // Target spell already left the stack (e.g. was countered by earlier effect on the same spell)
        log.info("Game {} - Target spell no longer on stack for life loss", gameData.id);
    }

    @HandlesEffect(EachOpponentLosesLifeEffect.class)
    private void resolveEachOpponentLosesLife(GameData gameData, StackEntry entry, EachOpponentLosesLifeEffect effect) {
        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            applyLifeLoss(gameData, playerId, effect.amount(), entry.getCard().getName());
        }
    }

    @HandlesEffect(EachOpponentLosesLifeAndControllerGainsLifeLostEffect.class)
    private void resolveEachOpponentLosesLifeAndControllerGainsLifeLost(GameData gameData, StackEntry entry,
                                                                        EachOpponentLosesLifeAndControllerGainsLifeLostEffect effect) {
        UUID controllerId = entry.getControllerId();
        int amount = effect.amount();

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            applyLifeLoss(gameData, playerId, amount, entry.getCard().getName());
            totalLifeLost += amount;
        }

        if (totalLifeLost > 0) {
            applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }

    @HandlesEffect(EachOpponentLosesXLifeAndControllerGainsLifeLostEffect.class)
    private void resolveEachOpponentLosesXLifeAndControllerGainsLifeLost(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        int x = entry.getXValue();

        if (x <= 0) {
            return;
        }

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            applyLifeLoss(gameData, playerId, x, entry.getCard().getName());
            totalLifeLost += x;
        }

        if (totalLifeLost > 0) {
            applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }

    @HandlesEffect(EachPlayerLosesLifeEffect.class)
    private void resolveEachPlayerLosesLife(GameData gameData, StackEntry entry, EachPlayerLosesLifeEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            applyLifeLoss(gameData, playerId, effect.amount(), entry.getCard().getName());
        }
    }

    @HandlesEffect(EachPlayerLosesLifePerCreatureControlledEffect.class)
    private void resolveEachPlayerLosesLifePerCreatureControlled(GameData gameData, StackEntry entry,
                                                                 EachPlayerLosesLifePerCreatureControlledEffect effect) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            int creatureCount = 0;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getType() == CardType.CREATURE) {
                    creatureCount++;
                }
            }

            int lifeLoss = creatureCount * effect.lifePerCreature();
            if (lifeLoss > 0) {
                applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        });
    }

    private void applyLifeLoss(GameData gameData, UUID playerId, int amount, String sourceName) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            return;
        }
        int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
        gameData.playerLifeTotals.put(playerId, currentLife - amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " loses " + amount + " life (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, amount, sourceName);

        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, amount);
    }

    @HandlesEffect(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class)
    private void resolveTargetPlayerLosesLifeAndControllerGainsLife(GameData gameData, StackEntry entry, TargetPlayerLosesLifeAndControllerGainsLifeEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID controllerId = entry.getControllerId();

        // Target loses life
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + "'s life total can't change.");
        } else {
            int targetCurrentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
            gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - effect.lifeLoss());

            String lossLog = targetName + " loses " + effect.lifeLoss() + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lossLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, effect.lifeLoss(), entry.getCard().getName());
        }

        // Controller gains life (skip if no life gain, e.g. pure life loss effects)
        if (effect.lifeGain() > 0) {
            applyGainLife(gameData, controllerId, effect.lifeGain());
        }
    }

    @HandlesEffect(DrainLifePerControlledPermanentEffect.class)
    private void resolveDrainLifePerControlledPermanent(GameData gameData, StackEntry entry, DrainLifePerControlledPermanentEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        UUID controllerId = entry.getControllerId();

        // Count matching permanents the controller controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        long count = battlefield == null ? 0 : battlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter()))
                .count();
        int drainAmount = (int) count * effect.multiplier();

        if (drainAmount <= 0) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + " drains 0 life (no matching permanents).");
            return;
        }

        // Target loses life
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + "'s life total can't change.");
        } else {
            int targetCurrentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
            gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - drainAmount);

            String lossLog = targetName + " loses " + drainAmount + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lossLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, drainAmount, entry.getCard().getName());
        }

        // Controller gains life
        applyGainLife(gameData, controllerId, drainAmount);
    }

    @HandlesEffect(TargetPlayerLosesLifeEffect.class)
    private void resolveTargetPlayerLosesLife(GameData gameData, StackEntry entry, TargetPlayerLosesLifeEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, targetName + "'s life total can't change.");
        } else {
            int targetCurrentLife = gameData.playerLifeTotals.getOrDefault(targetPlayerId, 20);
            gameData.playerLifeTotals.put(targetPlayerId, targetCurrentLife - effect.amount());

            String lossLog = targetName + " loses " + effect.amount() + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lossLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, targetName, effect.amount(), entry.getCard().getName());
        }
    }

    @HandlesEffect(TargetPlayerGainsLifeEffect.class)
    private void resolveTargetPlayerGainsLife(GameData gameData, StackEntry entry, TargetPlayerGainsLifeEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null) {
            return;
        }
        applyGainLife(gameData, targetPlayerId, effect.amount());
    }

    @HandlesEffect(GiveEnchantedPermanentControllerPoisonCountersEffect.class)
    private void resolveGiveEnchantedPermanentControllerPoisonCounters(GameData gameData, StackEntry entry,
                                                                       GiveEnchantedPermanentControllerPoisonCountersEffect effect) {
        UUID playerId = effect.affectedPlayerId();
        if (playerId == null) return;

        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + effect.amount());

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + effect.amount() + " poison counter" + (effect.amount() > 1 ? "s" : "")
                + " (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, effect.amount(), entry.getCard().getName());
    }

    @HandlesEffect(DamageSourceControllerGetsPoisonCounterEffect.class)
    private void resolveDamageSourceControllerGetsPoisonCounter(GameData gameData, StackEntry entry,
                                                                DamageSourceControllerGetsPoisonCounterEffect effect) {
        UUID playerId = effect.damageSourceControllerId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) return;
        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + 1);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets a poison counter (" + entry.getCard().getName() + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets a poison counter from {}", gameData.id, playerName, entry.getCard().getName());
    }

    @HandlesEffect(GiveControllerPoisonCountersEffect.class)
    private void resolveGiveControllerPoisonCounters(GameData gameData, StackEntry entry, GiveControllerPoisonCountersEffect effect) {
        applyPoisonCounters(gameData, entry.getControllerId(), effect.amount(), entry.getCard().getName());
    }

    @HandlesEffect(GiveEachPlayerPoisonCountersEffect.class)
    private void resolveGiveEachPlayerPoisonCounters(GameData gameData, StackEntry entry, GiveEachPlayerPoisonCountersEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            applyPoisonCounters(gameData, playerId, effect.amount(), entry.getCard().getName());
        }
    }

    @HandlesEffect(GiveTargetPlayerPoisonCountersEffect.class)
    private void resolveGiveTargetPlayerPoisonCounters(GameData gameData, StackEntry entry, GiveTargetPlayerPoisonCountersEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null) {
            return;
        }
        applyPoisonCounters(gameData, targetPlayerId, effect.amount(), entry.getCard().getName());
    }

    private void applyPoisonCounters(GameData gameData, UUID playerId, int amount, String sourceName) {
        if (!gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) return;

        int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
        gameData.playerPoisonCounters.put(playerId, currentPoison + amount);

        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " gets " + amount + " poison counter" + (amount > 1 ? "s" : "")
                + " (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName, amount, sourceName);
    }

    @HandlesEffect(GiveControllerPoisonCountersOnTargetDeathThisTurnEffect.class)
    private void resolveGiveControllerPoisonCountersOnTargetDeathThisTurn(GameData gameData, StackEntry entry, GiveControllerPoisonCountersOnTargetDeathThisTurnEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, delayed poison trigger not registered", gameData.id);
            return;
        }

        gameData.creatureGivingControllerPoisonOnDeathThisTurn.merge(
                target.getCard().getId(), effect.amount(), Integer::sum);

        log.info("Game {} - Delayed trigger registered: if {} dies this turn, its controller gets {} poison counter(s)",
                gameData.id, target.getCard().getName(), effect.amount());
    }

    @HandlesEffect(AwardRestrictedManaEffect.class)
    private void resolveAwardRestrictedMana(GameData gameData, StackEntry entry, AwardRestrictedManaEffect effect) {
        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        if (effect.color() == ManaColor.RED) {
            pool.addRestrictedRed(effect.amount());
        } else {
            pool.add(effect.color(), effect.amount());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " adds " + effect.amount() + " " + effect.color().getCode()
                + " (" + effect.allowedSpellTypes() + " spells only).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds {} {} (restricted to {})", gameData.id, playerName, effect.amount(), effect.color(), effect.allowedSpellTypes());
    }

    @HandlesEffect(AwardManaEffect.class)
    private void resolveAwardMana(GameData gameData, StackEntry entry, AwardManaEffect effect) {
        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        pool.add(effect.color(), effect.amount());

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " adds " + effect.amount() + " " + effect.color().getCode() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} adds {} {}", gameData.id, playerName, effect.amount(), effect.color());
    }

    @HandlesEffect(AddManaPerControlledSubtypeEffect.class)
    private void resolveAddManaPerControlledSubtype(GameData gameData, StackEntry entry, AddManaPerControlledSubtypeEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(effect.subtype())) {
                count++;
            }
        }

        ManaPool pool = gameData.playerManaPools.get(controllerId);
        for (int i = 0; i < count; i++) {
            pool.add(effect.color());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        CardSubtype subtype = effect.subtype();
        String logEntry = playerName + " adds " + count + " " + effect.color().getCode() + " (" + subtype.getDisplayName() + "s controlled).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} adds {} {} (per {} controlled)", gameData.id, playerName, count, effect.color(), subtype);
    }
}

