package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.WhirlwindDenialState;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WhirlwindDenialSupport {

    private static final String MANA_COST_PREFIX = "{";
    private static final List<StackEntryType> SPELL_TYPES = List.of(
            StackEntryType.CREATURE_SPELL, StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL, StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL,
            StackEntryType.BATTLE_SPELL);

    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    public void begin(GameData gameData, StackEntry source,
                      CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect effect) {
        WhirlwindDenialState state = gameData.whirlwindDenial;
        state.clear();
        state.sourceCard = source.getCard();
        state.sourceControllerId = source.getControllerId();
        state.amount = effect.amount();

        List<StackEntry> candidates = new ArrayList<>(gameData.stack);
        for (UUID playerId : opponentOrder(gameData, source.getControllerId())) {
            for (StackEntry candidate : candidates) {
                if (playerId.equals(candidate.getControllerId()) && isSpellOrAbility(candidate)) {
                    state.targetIds.add(candidate.getCard().getId());
                }
            }
        }

        offerNext(gameData);
    }

    public void handlePaymentChoice(GameData gameData, Player player, boolean accepted,
                                    PendingMayAbility ability) {
        WhirlwindDenialState state = gameData.whirlwindDenial;
        if (!state.active()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry target = findTarget(gameData, ability.targetCardId());
        StackEntry source = target == null ? null : sourceEntry(state);
        StackEntry legalTarget = target == null
                ? null
                : counterSupport.findCounterTarget(gameData, target.getCard().getId(), source);
        boolean paid = false;
        if (legalTarget != null && accepted) {
            ManaCost cost = new ManaCost(manaCost(state.amount));
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            if (pool != null && cost.canPay(pool)) {
                cost.pay(pool);
                paid = true;
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " pays " + manaCost(state.amount) + ". ",
                        target.getCard(), " is not countered."));
            }
        }

        if (!paid && legalTarget != null) {
            state.unpaidTargetIds.add(legalTarget.getCard().getId());
        }

        offerNext(gameData);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void offerNext(GameData gameData) {
        WhirlwindDenialState state = gameData.whirlwindDenial;
        StackEntry source = sourceEntry(state);
        while (state.nextTargetIndex < state.targetIds.size()) {
            UUID targetId = state.targetIds.get(state.nextTargetIndex++);
            StackEntry target = findTarget(gameData, targetId);
            if (target == null || counterSupport.findCounterTarget(gameData, targetId, source) == null) {
                continue;
            }

            ManaCost cost = new ManaCost(manaCost(state.amount));
            ManaPool pool = gameData.playerManaPools.get(target.getControllerId());
            if (pool == null || !cost.canPay(pool)) {
                state.unpaidTargetIds.add(targetId);
                continue;
            }

            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    state.sourceCard,
                    target.getControllerId(),
                    List.of(new CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect(state.amount)),
                    "Pay " + manaCost(state.amount) + " to prevent "
                            + target.getCard().getName() + " from being countered?",
                    targetId,
                    state.sourceControllerId));
            return;
        }

        finish(gameData, state, source);
    }

    private void finish(GameData gameData, WhirlwindDenialState state, StackEntry source) {
        List<UUID> unpaidTargetIds = new ArrayList<>(state.unpaidTargetIds);
        state.clear();
        for (UUID targetId : unpaidTargetIds) {
            StackEntry target = findTarget(gameData, targetId);
            if (target == null) {
                continue;
            }
            StackEntry legalTarget = counterSupport.findCounterTarget(gameData, targetId, source);
            if (legalTarget != null) {
                counterSupport.counterSpell(gameData, source, legalTarget);
            }
        }
    }

    private StackEntry sourceEntry(WhirlwindDenialState state) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, state.sourceCard, state.sourceControllerId,
                state.sourceCard.getName(), List.of());
    }

    private StackEntry findTarget(GameData gameData, UUID targetId) {
        if (targetId == null) {
            return null;
        }
        return gameData.stack.stream()
                .filter(stackEntry -> targetId.equals(stackEntry.getCard().getId()))
                .findFirst()
                .orElse(null);
    }

    private static boolean isSpellOrAbility(StackEntry entry) {
        return SPELL_TYPES.contains(entry.getEntryType())
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
    }

    private static List<UUID> opponentOrder(GameData gameData, UUID sourceControllerId) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(players.subList(activeIndex, players.size()));
            rotated.addAll(players.subList(0, activeIndex));
            players = rotated;
        }
        return players.stream().filter(playerId -> !playerId.equals(sourceControllerId)).toList();
    }

    private static String manaCost(int amount) {
        return MANA_COST_PREFIX + amount + "}";
    }
}
