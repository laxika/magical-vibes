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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Drives Tariff's per-player "pay a creature's mana cost or sacrifice it" sequence in APNAP order.
 *
 * <p>Each affected player is handled one at a time via {@code gameData.tariffRemainingPlayers}. For
 * a player, the creature with the greatest mana value is identified (a tie prompts that player to
 * choose one). If the player can pay that creature's mana cost, a may-ability prompt asks whether to
 * pay; declining — or being unable to pay — marks the creature, and every marked creature is
 * sacrificed at once after the last player.
 *
 * <p>Nothing changes before that sacrifice while the effect resolves without a prompt, so a creature
 * with competing death replacements stops and reruns the effect (CR 616.1). After a prompt the
 * payments already made can't be redone, so the final sacrifice alone is asked about first and
 * redone through a may answer without a creature, which only finishes the sequence.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TariffSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final DestructionSupport destructionSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Autowired
    @Lazy
    private PotentialManaService potentialManaService;

    /** Entry point from the effect handler: build the APNAP player queue and start processing. */
    public void begin(GameData gameData, Card sourceCard) {
        gameData.tariffRemainingPlayers.clear();
        gameData.tariffPendingSacrificeIds.clear();
        int start = Math.max(0, gameData.orderedPlayerIds.indexOf(gameData.activePlayerId));
        for (int offset = 0; offset < gameData.orderedPlayerIds.size(); offset++) {
            UUID playerId = gameData.orderedPlayerIds.get((start + offset) % gameData.orderedPlayerIds.size());
            if (!collectCreatures(gameData, playerId).isEmpty()) {
                gameData.tariffRemainingPlayers.add(playerId);
            }
        }
        processNextPlayer(gameData, sourceCard, null);
    }

    /**
     * Advance to the next player in the queue, or finish the effect when the queue is drained.
     * {@code answeringPlayerId} is the player whose answer to a prompt got here, or {@code null}
     * while the effect is still resolving.
     */
    private void processNextPlayer(GameData gameData, Card sourceCard, UUID answeringPlayerId) {
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

        finish(gameData, sourceCard, answeringPlayerId);
    }

    /** Resolve the choice for a single chosen creature. Returns true when a prompt is now pending. */
    public boolean resolveForCreature(GameData gameData, Card sourceCard, UUID playerId, Permanent creature) {
        String playerName = gameData.playerIdToName.get(playerId);
        ManaCost cost = creature.getCard().getParsedManaCost();
        ManaPool pool = gameData.playerManaPools.get(playerId);

        boolean canPay = cost != null && (cost.canPayResolutionCost(pool)
                || cost.canPay(potentialManaService.buildVirtualManaPool(gameData, playerId)));
        if (!canPay) {
            gameData.tariffPendingSacrificeIds.add(creature.getId());
            log.info("Game {} - {} can't pay for {} — to be sacrificed ({})", gameData.id, playerName,
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
            processNextPlayer(gameData, context.sourceCard(), context.playerId());
            return;
        }
        if (!resolveForCreature(gameData, context.sourceCard(), context.playerId(), creature)) {
            processNextPlayer(gameData, context.sourceCard(), context.playerId());
        }
    }

    /** May-ability completion: the player chose to pay (accepted) or sacrifice (declined). */
    public void handlePayOrSacrificeChoice(GameData gameData, Player player, boolean accepted,
            PendingMayAbility ability) {
        Card sourceCard = ability.sourceCard();
        UUID playerId = player.getId();
        Permanent creature = ability.targetCardId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, ability.targetCardId());

        if (creature == null) {
            // Creature is gone (or this only redoes the final sacrifice) — nothing to pay for or sacrifice.
            processNextPlayer(gameData, sourceCard, playerId);
            return;
        }

        ManaCost cost = new ManaCost(ability.manaCost());
        ManaPool pool = gameData.playerManaPools.get(playerId);

        if (accepted && cost.canPayResolutionCost(pool)) {
            cost.payResolutionCost(pool);
            
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " pays " + ability.manaCost() + " and keeps ").card(creature.getCard()).text(". (").card(sourceCard).text(")").build());
            log.info("Game {} - {} pays {} to keep {} ({})", gameData.id, player.getUsername(),
                    ability.manaCost(), creature.getCard().getName(), sourceCard.getName());
        } else {
            gameData.tariffPendingSacrificeIds.add(creature.getId());
            log.info("Game {} - {} declines to pay for {} ({})", gameData.id, player.getUsername(),
                    creature.getCard().getName(), sourceCard.getName());
        }

        processNextPlayer(gameData, sourceCard, playerId);
    }

    private void finish(GameData gameData, Card sourceCard, UUID answeringPlayerId) {
        if (answeringPlayerId == null) {
            // Still inside the effect's own resolution, whose rerun window covers the sacrifice;
            // returning lets EffectResolutionService finalize the spell.
            sacrificeMarkedCreatures(gameData);
            return;
        }
        // Reached via a completed prompt: the final sacrifice is asked about first and redone by
        // answering a creatureless prompt, then the spell is finalized (SBA, graveyard).
        PendingMayAbility finishOnly = new PendingMayAbility(sourceCard, answeringPlayerId,
                List.of(new EachPlayerSacrificesGreatestManaValueCreatureUnlessPaysEffect()),
                "", null, (String) null);
        permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(gameData,
                new Player(answeringPlayerId, gameData.playerIdToName.get(answeringPlayerId)), finishOnly, false,
                () -> {
                    sacrificeMarkedCreatures(gameData);
                    inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                });
    }

    /** Kept until the sacrifice is done, so a stop for a death replacement can sacrifice them again. */
    private void sacrificeMarkedCreatures(GameData gameData) {
        if (!gameData.tariffPendingSacrificeIds.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, List.copyOf(gameData.tariffPendingSacrificeIds));
            gameData.tariffPendingSacrificeIds.clear();
        }
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
