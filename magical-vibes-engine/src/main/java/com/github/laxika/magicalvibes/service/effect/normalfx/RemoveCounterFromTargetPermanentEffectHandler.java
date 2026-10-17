package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveCounterFromTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final BattleDefeatSupport battleDefeatSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromTargetPermanentEffect) effect;
        if (e.upTo() && gameData.chosenXValue != null) {
            int chosen = Math.max(0, gameData.chosenXValue);
            gameData.chosenXValue = null;
            Permanent chosenTarget = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (chosenTarget != null) {
                removeUpTo(gameData, chosenTarget, e.counterType(), chosen);
            }
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Specific counter type ("remove a -1/-1 counter", "remove up to four charge counters"):
        // remove as many of exactly that type as are there, up to the effect's amount.
        if (e.counterType() != null) {
            if (e.upTo()) {
                int maxRemovable = Math.min(e.amount(), target.getCounterCount(e.counterType()));
                if (maxRemovable > 0) {
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                            entry.getControllerId(), 0, maxRemovable,
                            "Remove up to " + maxRemovable + " "
                                    + permanentCounterSupport.counterTypeName(e.counterType())
                                    + " counters from " + target.getCard().getName() + ".",
                            entry.getCard().getName()));
                }
                return;
            }
            removeUpTo(gameData, target, e.counterType(), e.amount());
            return;
        }

        // "Remove a counter": the ability's controller chooses the kind when several are present.
        List<CounterType> kinds = RemoveChosenCountersFromTargetPermanentEffectHandler.counterTypesOn(target);
        if (kinds.size() > 1 && e.amount() == 1) {
            playerInputService.beginRemoveOneCounterChoice(gameData, entry.getControllerId(), target.getId(),
                    entry.getCard().getName(), kinds, false);
            return;
        }
        for (CounterType counterType : kinds) {
            if (removeUpTo(gameData, target, counterType, e.amount())) {
                return;
            }
        }
    }

    /** Removes up to {@code amount} counters of one type; returns whether any came off. */
    private boolean removeUpTo(GameData gameData, Permanent target, CounterType counterType, int amount) {
        int removed = Math.min(amount, target.getCounterCount(counterType));
        if (removed <= 0) {
            return false;
        }

        target.setCounterCount(counterType, target.getCounterCount(counterType) - removed);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(target, removed);
        }
        String counterName = permanentCounterSupport.counterTypeName(counterType);
        String prefix = removed == 1
                ? "A " + counterName + " counter removed from "
                : removed + " " + counterName + " counters removed from ";
        gameLogService.append(gameData, GameLog.textCardText(prefix, target.getCard(), "."));
        log.info("Game {} - {} {} counter(s) removed from {}", gameData.id, removed, counterType, target.getCard().getName());
        if (counterType == CounterType.DEFENSE) {
            battleDefeatSupport.checkAfterDefenseRemoved(gameData, target);
        }
        return true;
    }
}
