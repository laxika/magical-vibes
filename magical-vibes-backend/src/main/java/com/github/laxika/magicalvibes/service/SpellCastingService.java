package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastingService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final TargetLegalityService targetLegalityService;

    void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments,
                  List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds, boolean fromGraveyard) {
        int effectiveXValue = xValue != null ? xValue : 0;
        if (targetPermanentIds == null) targetPermanentIds = List.of();
        if (convokeCreatureIds == null) convokeCreatureIds = List.of();
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }

        UUID playerId = player.getId();

        // Handle playing a land from graveyard (e.g. via Crucible of Worlds)
        if (fromGraveyard) {
            List<Integer> playableGraveyard = gameBroadcastService.getPlayableGraveyardLandIndices(gameData, playerId);
            if (!playableGraveyard.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable from graveyard");
            }
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            Card graveyardCard = graveyard.get(cardIndex);
            if (graveyardCard.getType() != CardType.LAND) {
                throw new IllegalStateException("Only lands can be played from graveyard");
            }
            graveyard.remove(cardIndex);
            gameData.playerBattlefields.get(playerId).add(new Permanent(graveyardCard));
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

            String logEntry = player.getUsername() + " plays " + graveyardCard.getName() + " from graveyard.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} plays {} from graveyard", gameData.id, player.getUsername(), graveyardCard.getName());

            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        List<Integer> playable = gameBroadcastService.getPlayableCardIndices(gameData, playerId);
        if (!playable.contains(cardIndex)) {
            // Re-check with convoke if card has convoke keyword
            List<Card> handCheck = gameData.playerHands.get(playerId);
            Card cardCheck = handCheck.get(cardIndex);
            if (!cardCheck.getKeywords().contains(Keyword.CONVOKE) || convokeCreatureIds.isEmpty()) {
                throw new IllegalStateException("Card is not playable");
            }
            // Allow convoke-assisted casting even if not in basic playable list
            List<Integer> convokePlayable = gameBroadcastService.getPlayableCardIndices(gameData, playerId, convokeCreatureIds.size());
            if (!convokePlayable.contains(cardIndex)) {
                throw new IllegalStateException("Card is not playable even with convoke");
            }
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.get(cardIndex);
        List<com.github.laxika.magicalvibes.model.effect.CardEffect> filteredSpellEffects =
                new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        boolean usesSacrificeAllCreaturesCost = filteredSpellEffects.stream()
                .anyMatch(e -> e instanceof SacrificeAllCreaturesYouControlCost);
        if (usesSacrificeAllCreaturesCost) {
            filteredSpellEffects.removeIf(SacrificeAllCreaturesYouControlCost.class::isInstance);
        }

        // For X-cost spells, validate that player can pay colored + generic + xValue + any cost increases
        if (card.getManaCost() != null) {
            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.hasX()) {
                if (effectiveXValue < 0) {
                    throw new IllegalStateException("X value cannot be negative");
                }
                ManaPool pool = gameData.playerManaPools.get(playerId);
                int additionalCost = gameBroadcastService.getOpponentCostIncrease(gameData, playerId, card.getType());
                if (card.getXColorRestriction() != null) {
                    if (!cost.canPay(pool, effectiveXValue, card.getXColorRestriction(), additionalCost)) {
                        throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                    }
                } else if (!cost.canPay(pool, effectiveXValue + additionalCost)) {
                    throw new IllegalStateException("Not enough mana to pay for X=" + effectiveXValue);
                }
            }
        }

        // Validate spell target (targeting a spell on the stack)
        if (card.isNeedsSpellTarget()) {
            targetLegalityService.validateSpellTargetOnStack(gameData, targetPermanentId, card.getTargetFilter());
        }

        boolean needsSingleGraveyardCreatureTargeting = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof ReturnCreatureFromGraveyardToHandEffect);

        // Validate target if specified (can be a permanent or a player)
        if (targetPermanentId != null && !card.isNeedsSpellTarget()) {
            if (needsSingleGraveyardCreatureTargeting) {
                boolean inControllersGraveyard = gameData.playerGraveyards
                        .getOrDefault(playerId, List.of())
                        .stream()
                        .anyMatch(c -> c.getId().equals(targetPermanentId));
                if (!inControllersGraveyard) {
                    throw new IllegalStateException("Target must be a creature card in your graveyard");
                }
                targetLegalityService.validateEffectTargetInZone(gameData, card, targetPermanentId, Zone.GRAVEYARD);
            } else {
                targetLegalityService.validateSpellTargeting(gameData, card, targetPermanentId, null, playerId);
            }
        } else if (card.isNeedsTarget() && needsSingleGraveyardCreatureTargeting) {
            throw new IllegalStateException("Must target a creature card in your graveyard");
        }

        // Validate multi-target permanent targeting
        if (card.getMaxTargets() > 0 && !targetPermanentIds.isEmpty()) {
            targetLegalityService.validateMultiSpellTargets(gameData, card, targetPermanentIds);
        }

        // Validate and apply convoke
        List<ManaColor> convokeContributions = List.of();
        if (!convokeCreatureIds.isEmpty() && card.getKeywords().contains(Keyword.CONVOKE)) {
            List<ManaColor> contributions = new ArrayList<>();
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            for (UUID creatureId : convokeCreatureIds) {
                Permanent creature = battlefield.stream()
                        .filter(p -> p.getId().equals(creatureId))
                        .findFirst()
                        .orElse(null);
                if (creature == null) {
                    throw new IllegalStateException("Convoke creature not found on your battlefield");
                }
                if (!gameQueryService.isCreature(gameData, creature)) {
                    throw new IllegalStateException(creature.getCard().getName() + " is not a creature");
                }
                if (creature.isTapped()) {
                    throw new IllegalStateException(creature.getCard().getName() + " is already tapped");
                }
                // Each creature contributes one mana of any of its colors, or colorless (generic only)
                CardColor creatureColor = creature.getCard().getColor();
                if (creatureColor != null) {
                    contributions.add(ManaColor.fromCode(creatureColor.getCode()));
                } else {
                    // Colorless creature can only pay generic
                    contributions.add(null);
                }
            }
            // Tap all convoke creatures
            for (UUID creatureId : convokeCreatureIds) {
                Permanent creature = battlefield.stream()
                        .filter(p -> p.getId().equals(creatureId))
                        .findFirst()
                        .orElse(null);
                if (creature != null) {
                    creature.tap();
                }
            }
            convokeContributions = contributions;
        }

        // Validate graveyard targets for spells that target creature cards in graveyard
        boolean needsGraveyardCreatureTargeting = card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof ExileCreaturesFromGraveyardAndCreateTokensEffect);
        if (needsGraveyardCreatureTargeting && effectiveXValue > 0) {
            long creatureCount = gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(c -> c.getType() == CardType.CREATURE)
                    .count();
            if (effectiveXValue > creatureCount) {
                throw new IllegalStateException("Not enough creature cards in graveyard (need " + effectiveXValue + ", have " + creatureCount + ")");
            }
        }

        hand.remove(cardIndex);

        if (card.getType() == CardType.LAND) {
            // Lands bypass the stack — go directly onto battlefield
            gameData.playerBattlefields.get(playerId).add(new Permanent(card));
            gameData.landsPlayedThisTurn.merge(playerId, 1, Integer::sum);

            String logEntry = player.getUsername() + " plays " + card.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} plays {}", gameData.id, player.getUsername(), card.getName());

            turnProgressionService.resolveAutoPass(gameData);
        } else if (card.getType() == CardType.CREATURE) {
            paySpellManaCost(gameData, playerId, card, 0, convokeContributions);
            gameData.stack.add(new StackEntry(
                    StackEntryType.CREATURE_SPELL, card, playerId, card.getName(),
                    List.of(), 0, targetPermanentId, null
            ));
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (card.getType() == CardType.ENCHANTMENT) {
            paySpellManaCost(gameData, playerId, card, 0, convokeContributions);
            gameData.stack.add(new StackEntry(
                    StackEntryType.ENCHANTMENT_SPELL, card, playerId, card.getName(),
                    List.of(), 0, targetPermanentId, null
            ));
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (card.getType() == CardType.ARTIFACT) {
            paySpellManaCost(gameData, playerId, card, 0, convokeContributions);
            gameData.stack.add(new StackEntry(
                    StackEntryType.ARTIFACT_SPELL, card, playerId, card.getName(),
                    List.of(), 0, null, null
            ));
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (card.getType() == CardType.PLANESWALKER) {
            paySpellManaCost(gameData, playerId, card, 0, convokeContributions);
            gameData.stack.add(new StackEntry(
                    StackEntryType.PLANESWALKER_SPELL, card, playerId, card.getName(),
                    List.of(), 0, null, null
            ));
            finishSpellCast(gameData, playerId, player, hand, card);
        } else if (card.getType() == CardType.SORCERY) {
            int resolvedXValue = effectiveXValue;
            paySpellManaCost(gameData, playerId, card, resolvedXValue, convokeContributions);
            if (usesSacrificeAllCreaturesCost) {
                resolvedXValue = paySacrificeAllCreaturesYouControlCost(
                        gameData, player, card
                );
            }
            if (needsGraveyardCreatureTargeting && resolvedXValue > 0) {
                // Prompt player to choose graveyard targets before putting spell on stack
                gameHelper.handleGraveyardSpellTargeting(gameData, playerId, card,
                        StackEntryType.SORCERY_SPELL, resolvedXValue);
            } else if (needsGraveyardCreatureTargeting) {
                // X=0: no targets needed, put spell on stack directly (resolves doing nothing)
                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, 0, null,
                        null, null, null, List.of(), List.of()
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else if (!targetPermanentIds.isEmpty() && !usesSacrificeAllCreaturesCost) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentIds
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else if (needsSingleGraveyardCreatureTargeting) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentId, null,
                        Map.of(), Zone.GRAVEYARD, List.of(), List.of()
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.SORCERY_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentId, null
                ));
                finishSpellCast(gameData, playerId, player, hand, card);
            }
        } else if (card.getType() == CardType.INSTANT) {
            int resolvedXValue = effectiveXValue;
            paySpellManaCost(gameData, playerId, card, resolvedXValue, convokeContributions);
            if (usesSacrificeAllCreaturesCost) {
                resolvedXValue = paySacrificeAllCreaturesYouControlCost(
                        gameData, player, card
                );
            }

            // Validate damage assignments for damage distribution spells
            if (card.isNeedsDamageDistribution()) {
                if (damageAssignments == null || damageAssignments.isEmpty()) {
                    throw new IllegalStateException("Damage assignments required");
                }
                int totalDamage = damageAssignments.values().stream().mapToInt(Integer::intValue).sum();
                if (totalDamage != resolvedXValue) {
                    throw new IllegalStateException("Damage assignments must sum to X (" + resolvedXValue + ")");
                }
                for (Map.Entry<UUID, Integer> assignment : damageAssignments.entrySet()) {
                    Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
                    if (target == null || !gameQueryService.isCreature(gameData, target) || !target.isAttacking()) {
                        throw new IllegalStateException("All targets must be attacking creatures");
                    }
                    if (assignment.getValue() <= 0) {
                        throw new IllegalStateException("Each damage assignment must be positive");
                    }
                }

                gameData.stack.add(new StackEntry(
                        StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, null, damageAssignments
                ));
            } else if (card.isNeedsSpellTarget()) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, targetPermanentId, Zone.STACK
                ));
            } else if (!targetPermanentIds.isEmpty() && !usesSacrificeAllCreaturesCost) {
                // Multi-target spell (e.g. "one or two target creatures each get +2/+1")
                gameData.stack.add(new StackEntry(
                        StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentIds
                ));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.INSTANT_SPELL, card, playerId, card.getName(),
                        filteredSpellEffects, resolvedXValue, targetPermanentId, null
                ));
            }
            finishSpellCast(gameData, playerId, player, hand, card);
        }
    }

    private int paySacrificeAllCreaturesYouControlCost(
            GameData gameData,
            Player player,
            Card sourceCard
    ) {
        UUID playerId = player.getId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<Permanent> creaturesToSacrifice = battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();

        // Snapshot total power first, because all chosen creatures are sacrificed together.
        int totalPower = 0;
        for (Permanent creature : creaturesToSacrifice) {
            totalPower += gameQueryService.getEffectivePower(gameData, creature);
        }
        for (Permanent creature : creaturesToSacrifice) {
            if (gameHelper.removePermanentToGraveyard(gameData, creature)) {
                String logEntry = player.getUsername() + " sacrifices " + creature.getCard().getName()
                        + " for " + sourceCard.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }
        return Math.max(0, totalPower);
    }

    void paySpellManaCost(GameData gameData, UUID playerId, Card card, int effectiveXValue, List<ManaColor> convokeContributions) {
        if (card.getManaCost() == null) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int additionalCost = gameBroadcastService.getOpponentCostIncrease(gameData, playerId, card.getType());
        if (!convokeContributions.isEmpty()) {
            cost.payWithConvoke(pool, additionalCost, convokeContributions);
        } else if (cost.hasX() && card.getXColorRestriction() != null) {
            cost.pay(pool, effectiveXValue, card.getXColorRestriction(), additionalCost);
        } else if (cost.hasX()) {
            cost.pay(pool, effectiveXValue + additionalCost);
        } else {
            cost.pay(pool, additionalCost);
        }
    }

    void finishSpellCast(GameData gameData, UUID playerId, Player player, List<Card> hand, Card card) {
        gameData.spellsCastThisTurn.merge(playerId, 1, Integer::sum);
        gameData.priorityPassedBy.clear();

        String logEntry = player.getUsername() + " casts " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} casts {}", gameData.id, player.getUsername(), card.getName());

        gameHelper.checkSpellCastTriggers(gameData, card);
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }

}

