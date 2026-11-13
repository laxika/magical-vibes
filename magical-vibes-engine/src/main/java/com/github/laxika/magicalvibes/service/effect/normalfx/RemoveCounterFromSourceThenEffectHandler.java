package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a counter removal followed by a reflexive effect. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceThenEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        if (e.counterType() == CounterType.ANY) {
            var options = java.util.Arrays.stream(CounterType.values())
                    .filter(type -> type != CounterType.ANY && type != CounterType.SILVER
                            && source.getCounterCount(type) >= e.count())
                    .map(type -> new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                            "Remove " + e.count() + " " + permanentCounterSupport.counterTypeName(type) + " counters",
                            new RemoveCounterFromSourceThenEffect(type, e.count(), e.thenEffect(),
                                    e.onlyIfLastCounterRemoved(), e.reflexive())))
                    .toList();
            if (options.size() > 1) {
                playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                        new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(options),
                        false, entry.getSourcePermanentId());
                return;
            }
        }
        CounterType counterType = findCounterType(source, e.counterType(), e.count());
        if (counterType == null) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - e.count());
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(source, e.count());
        }
        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" removes ").text(e.count() == 1 ? "a " : e.count() + " ")
                .text(permanentCounterSupport.counterTypeName(counterType))
                .text(e.count() == 1 ? " counter." : " counters.").build());

        if (!e.onlyIfLastCounterRemoved() || source.getCounterCount(counterType) == 0) {
            if (!e.reflexive() && entry.getResolvingEffectIndex() >= 0) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(e.thenEffect()));
            } else {
                beginReflexiveTrigger(gameData, entry, e.thenEffect());
            }
        }
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        // Whichever way the ability reaches the stack, it records what the resolving entry was about.
        StackEntry record = ReflexiveAbilitySupport.record(
                gameData, entry, List.of(thenEffect), entry.getCard().getName() + "'s ability");
        if (record.getAttackedTargetId() == null) {
            // "Defending player" of the ability's target is the player the resolving entry was about.
            record.setAttackedTargetId(entry.getTargetId());
        }
        // "When you do" is an ability of its own (CR 603.12): its mode and targets are chosen as it goes on the stack.
        triggeredAbilityStackService.putMade(gameData, record);
    }

    private CounterType findCounterType(Permanent source, CounterType requestedType, int count) {
        if (requestedType != CounterType.ANY) {
            return source.getCounterCount(requestedType) >= count ? requestedType : null;
        }
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            if (source.getCounterCount(counterType) >= count) {
                return counterType;
            }
        }
        return null;
    }
}
