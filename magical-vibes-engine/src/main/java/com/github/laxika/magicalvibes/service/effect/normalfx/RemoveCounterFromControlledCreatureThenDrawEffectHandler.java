package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureThenDrawEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveCounterFromControlledCreatureThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DrawCardEffectHandler drawCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromControlledCreatureThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> eligibleIds = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> firstPresentCounterType(permanent) != null)
                .map(Permanent::getId)
                .toList();

        if (eligibleIds.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RemoveCounterFromControlledCreatureThenDraw(entry));
        playerInputService.beginPermanentChoice(
                gameData, controllerId, eligibleIds,
                "Choose a creature you control from which to remove a counter.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.RemoveCounterFromControlledCreatureThenDraw context) {
        StackEntry entry = context.resolvingEntry();
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, permanent)) {
            return;
        }

        CounterType counterType = firstPresentCounterType(permanent);
        if (counterType == null) {
            return;
        }

        permanentCounterSupport.removeCounterFromPermanent(gameData, permanent, counterType, 1);
        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect());
    }

    private CounterType firstPresentCounterType(Permanent permanent) {
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY
                    && counterType != CounterType.SILVER
                    && permanent.getCounterCount(counterType) > 0) {
                return counterType;
            }
        }
        return null;
    }
}
