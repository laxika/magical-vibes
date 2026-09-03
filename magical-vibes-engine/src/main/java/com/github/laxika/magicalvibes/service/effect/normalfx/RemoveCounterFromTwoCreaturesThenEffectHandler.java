package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTwoCreaturesThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the exact-two-creature counter-removal choice and its contingent effect. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromTwoCreaturesThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromTwoCreaturesThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RemoveCounterFromTwoCreaturesThenEffect) effect;
        List<UUID> eligibleIds = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> permanent.getCounterCount(typedEffect.counterType()) > 0)
                .map(Permanent::getId)
                .toList();

        if (eligibleIds.size() < 2) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                eligibleIds,
                2,
                new MultiPermanentChoiceContext.RemoveCounterFromTwoCreatures(
                        entry, typedEffect.counterType(), typedEffect.thenEffect()),
                "Choose exactly two creatures you control from which to remove a counter.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.RemoveCounterFromTwoCreatures context) {
        UUID controllerId = context.resolvingEntry().getControllerId();
        int removedCount = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null
                    || !controllerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                    || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }

            int currentCount = permanent.getCounterCount(context.counterType());
            if (currentCount > 0) {
                permanent.setCounterCount(context.counterType(), currentCount - 1);
                removedCount++;
            }
        }

        if (removedCount == 2) {
            context.resolvingEntry().insertEffectsToResolve(
                    gameData.pendingEffectResolutionIndex, List.of(context.thenEffect()));
        }
    }
}
