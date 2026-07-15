package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Drives Tariff's per-player "pay a creature's mana cost or sacrifice it" sequence in APNAP order.
 *
 * <p>Each affected player is handled one at a time via {@code gameData.tariffRemainingPlayers}. For
 * a player, the creature with the greatest mana value is identified (a tie prompts that player to
 * choose one). If the player can pay that creature's mana cost, a may-ability prompt asks whether to
 * pay; declining — or being unable to pay — sacrifices the creature.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TariffSupport {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final DestructionSupport destructionSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    /** Entry point from the effect handler: build the APNAP player queue and start processing. */
    public void begin(GameData gameData, Card sourceCard) {
        gameData.tariffRemainingPlayers.clear();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!collectCreatures(gameData, playerId).isEmpty()) {
                gameData.tariffRemainingPlayers.add(playerId);
            }
        }
        processNextPlayer(gameData, sourceCard);
    }

    /** Advance to the next player in the queue, or finish the effect when the queue is drained. */
    public void processNextPlayer(GameData gameData, Card sourceCard) {
        while (!gameData.tariffRemainingPlayers.isEmpty()) {
            UUID playerId = gameData.tariffRemainingPlayers.removeFirst();
            List<Permanent> creatures = collectCreatures(gameData, playerId);
            if (creatures.isEmpty()) {
                continue;
            }

            int maxManaValue = creatures.stream()
                    .mapToInt(p -> p.getCard().getManaValue())
                    .max().orElse(0);
            List<Permanent> tied = creatures.stream()
                    .filter(p -> p.getCard().getManaValue() == maxManaValue)
                    .toList();

            if (tied.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.TariffTieBreak(playerId, sourceCard));
                playerInputService.beginPermanentChoice(gameData, playerId,
                        tied.stream().map(Permanent::getId).toList(),
                        "Choose which creature you must pay for or sacrifice (" + sourceCard.getName() + ").");
                return;
            }

            // Exactly one creature at the greatest mana value — resolve it. If this begins a
            // may-ability prompt, stop here; otherwise (auto-sacrifice) the loop continues.
            if (resolveForCreature(gameData, sourceCard, playerId, tied.getFirst())) {
                return;
            }
        }

        finish(gameData);
    }

    /** Resolve the choice for a single chosen creature. Returns true when a prompt is now pending. */
    public boolean resolveForCreature(GameData gameData, Card sourceCard, UUID playerId, Permanent creature) {
        String playerName = gameData.playerIdToName.get(playerId);
        ManaCost cost = creature.getCard().getParsedManaCost();
        ManaPool pool = gameData.playerManaPools.get(playerId);

        if (cost == null || !cost.canPay(pool)) {
            destructionSupport.sacrificeAndLog(gameData, creature, playerId);
            log.info("Game {} - {} can't pay for {} — sacrificed ({})", gameData.id, playerName,
                    creature.getCard().getName(), sourceCard.getName());
            return false;
        }

        String prompt = "Pay " + creature.getCard().getManaCost() + " or sacrifice "
                + creature.getCard().getName() + "? (" + sourceCard.getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard, playerId,
                List.of(new EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect()),
                prompt, creature.getId(), creature.getCard().getManaCost()));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    /** Tie-break completion: the player picked which tied creature is at risk. */
    public void handleTieBreakChosen(GameData gameData, UUID permanentId,
            PermanentChoiceContext.TariffTieBreak context) {
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        if (creature == null) {
            processNextPlayer(gameData, context.sourceCard());
            return;
        }
        if (!resolveForCreature(gameData, context.sourceCard(), context.playerId(), creature)) {
            processNextPlayer(gameData, context.sourceCard());
        }
    }

    /** May-ability completion: the player chose to pay (accepted) or sacrifice (declined). */
    public void handlePayOrSacrificeChoice(GameData gameData, Player player, boolean accepted,
            PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();
        UUID playerId = player.getId();
        Permanent creature = gameQueryService.findPermanentById(gameData, ability.targetCardId());

        if (creature == null) {
            // Creature is gone — nothing to pay for or sacrifice.
            processNextPlayer(gameData, sourceCard);
            return;
        }

        ManaCost cost = new ManaCost(ability.manaCost());
        ManaPool pool = gameData.playerManaPools.get(playerId);

        if (accepted && cost.canPay(pool)) {
            cost.pay(pool);
            String logEntry = player.getUsername() + " pays " + ability.manaCost() + " and keeps "
                    + creature.getCard().getName() + ". (" + sourceCard.getName() + ")";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} pays {} to keep {} ({})", gameData.id, player.getUsername(),
                    ability.manaCost(), creature.getCard().getName(), sourceCard.getName());
        } else {
            destructionSupport.sacrificeAndLog(gameData, creature, playerId);
            log.info("Game {} - {} sacrifices {} ({})", gameData.id, player.getUsername(),
                    creature.getCard().getName(), sourceCard.getName());
        }

        processNextPlayer(gameData, sourceCard);
    }

    private void finish(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            // Reached asynchronously via a completed prompt — finalize the spell (SBA, graveyard).
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
        // Otherwise we are still inside the initial synchronous effect resolution; returning lets
        // EffectResolutionService finalize the spell.
    }

    private List<Permanent> collectCreatures(GameData gameData, UUID playerId) {
        List<Permanent> creatures = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield != null) {
            for (Permanent perm : battlefield) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    creatures.add(perm);
                }
            }
        }
        return creatures;
    }
}
