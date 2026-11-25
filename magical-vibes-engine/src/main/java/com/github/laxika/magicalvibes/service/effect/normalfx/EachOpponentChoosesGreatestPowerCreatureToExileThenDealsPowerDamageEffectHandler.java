package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Olórin's Searing Light's per-opponent exile choices and spell-mastery damage. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final ConditionEvaluationService conditionEvaluationService;
    private final DamageSupport damageSupport;
    private final ExileSupport exileSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var searingLight =
                (EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect) effect;
        beginNextOpponent(gameData, entry, apnapOpponents(gameData, entry.getControllerId()),
                searingLight.damageCondition(), Map.of());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamage context) {
        StackEntry entry = context.resolvingEntry();
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !greatestPowerCreatures(gameData, context.opponentId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(permanentId))) {
            throw new IllegalStateException(
                    "Chosen permanent is no longer a greatest-power creature controlled by the opponent");
        }

        Map<UUID, UUID> chosenCreatureIds = new java.util.LinkedHashMap<>(context.chosenCreatureIds());
        chosenCreatureIds.put(context.opponentId(), permanentId);
        beginNextOpponent(gameData, entry, context.remainingOpponentIds(), context.damageCondition(),
                chosenCreatureIds);
    }

    /**
     * Asks the next opponent with tied greatest-power creatures, in APNAP order, each knowing the earlier choices;
     * an opponent with one such creature chooses it unasked. Once every opponent has chosen, the creatures are
     * exiled together (its ruling; CR 101.4).
     */
    private void beginNextOpponent(GameData gameData, StackEntry entry, List<UUID> remainingOpponentIds,
                                   Condition damageCondition,
                                   Map<UUID, UUID> chosenCreatureIds) {
        Map<UUID, UUID> chosen = new java.util.LinkedHashMap<>(chosenCreatureIds);
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<UUID> candidates = greatestPowerCreatures(gameData, opponentId).stream()
                    .map(Permanent::getId).toList();
            if (candidates.size() > 1) {
                PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamage context =
                        new PermanentChoiceContext.EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamage(
                                entry, opponentId, List.copyOf(remaining), damageCondition, chosen);
                gameData.interaction.setPermanentChoiceContext(context);
                playerInputService.beginPermanentChoice(gameData, opponentId, candidates, context,
                        entry.getCard().getName() + " — choose a creature with the greatest power to exile.");
                return;
            }
            if (!candidates.isEmpty()) {
                chosen.put(opponentId, candidates.getFirst());
            }
        }
        finish(gameData, entry, damageCondition, exileChosen(gameData, entry, chosen));
    }

    /**
     * Exiles each opponent's chosen creature still on the battlefield, all as one event, and returns the power each
     * had as it last existed on the battlefield (its ruling), by opponent.
     */
    private Map<UUID, Integer> exileChosen(GameData gameData, StackEntry entry, Map<UUID, UUID> chosenCreatureIds) {
        Map<UUID, Integer> exiledPowers = new java.util.HashMap<>();
        Map<UUID, Permanent> toExile = new java.util.LinkedHashMap<>();
        chosenCreatureIds.forEach((opponentId, permanentId) -> {
            Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
            if (creature != null
                    && opponentId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                toExile.put(opponentId, creature);
                exiledPowers.put(opponentId, Math.max(0, gameQueryService.getEffectivePower(gameData, creature)));
            }
        });
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            toExile.values().forEach(creature ->
                    exileSupport.exilePermanentAndLog(gameData, creature, entry.getCard().getName()));
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        return exiledPowers;
    }

    private void finish(GameData gameData, StackEntry entry,
                        Condition damageCondition,
                        Map<UUID, Integer> exiledPowers) {
        if (!conditionEvaluationService.isMet(gameData, damageCondition,
                ConditionContext.forStackEntry(entry), entry.getEventValue())
                || damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(entry.getControllerId())) {
                continue;
            }
            int power = exiledPowers.getOrDefault(opponentId, 0);
            if (power <= 0) {
                continue;
            }
            int damage = power;
            damageSupport.dealDamageToPlayer(gameData, entry, opponentId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    private List<Permanent> greatestPowerCreatures(GameData gameData, UUID playerId) {
        List<Permanent> creatures = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .toList();
        if (creatures.isEmpty()) {
            return List.of();
        }

        int greatestPower = creatures.stream()
                .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                .max()
                .orElseThrow();
        return creatures.stream()
                .filter(permanent -> gameQueryService.getEffectivePower(gameData, permanent) == greatestPower)
                .toList();
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex >= 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return ordered.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
