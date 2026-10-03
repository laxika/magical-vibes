package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveChosenCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a counter choice for an entering creature trigger. */
@Component
@RequiredArgsConstructor
public class MoveChosenCounterFromSourceToEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveChosenCounterFromSourceToEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MoveChosenCounterFromSourceToEnteringCreatureEffect moveEffect =
                (MoveChosenCounterFromSourceToEnteringCreatureEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent enteringCreature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || enteringCreature == null) {
            return;
        }

        List<CounterType> availableCounterTypes = moveEffect.counterTypes().stream()
                .filter(counterType -> counterType != CounterType.ANY && counterType != CounterType.SILVER)
                .filter(counterType -> source.getCounterCount(counterType) > 0)
                .toList();
        if (availableCounterTypes.isEmpty()) {
            return;
        }
        if (availableCounterTypes.size() == 1) {
            moveCounter(gameData, entry, source, enteringCreature, availableCounterTypes.getFirst());
            return;
        }

        playerInputService.beginMoveOneCounterChoice(
                gameData, entry.getControllerId(), source.getId(), enteringCreature.getId(),
                entry.getCard().getName(), availableCounterTypes);
    }

    private void moveCounter(GameData gameData, StackEntry entry, Permanent source,
                             Permanent enteringCreature, CounterType counterType) {
        if (source.getCounterCount(counterType) <= 0) {
            return;
        }
        if (permanentCounterSupport.placeCounterOnPermanent(gameData, entry, enteringCreature, counterType, 1) > 0) {
            source.setCounterCount(counterType, source.getCounterCount(counterType) - 1);
        }
    }
}
