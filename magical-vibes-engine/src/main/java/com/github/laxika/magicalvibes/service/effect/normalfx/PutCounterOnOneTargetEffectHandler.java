package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOneTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a counter placement choice among the surviving targets of a multi-target effect. */
@Component
@RequiredArgsConstructor
public class PutCounterOnOneTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnOneTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var counterEffect = (PutCounterOnOneTargetEffect) effect;
        List<UUID> legalTargets = entry.targetsForEffect(effect).stream()
                .filter(targetId -> {
                    Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                    return target != null && gameQueryService.isCreature(gameData, target);
                })
                .distinct()
                .toList();

        UUID chosenTargetId = entry.getTargetId();
        if (chosenTargetId != null) {
            if (legalTargets.contains(chosenTargetId)) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry,
                        gameQueryService.findPermanentById(gameData, chosenTargetId),
                        counterEffect.counterType(), 1);
            }
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        if (legalTargets.size() <= 1) {
            if (legalTargets.size() == 1) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry,
                        gameQueryService.findPermanentById(gameData, legalTargets.getFirst()),
                        counterEffect.counterType(), 1);
            }
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ResolvingModalTarget(entry.getCard(), entry.getControllerId()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), legalTargets,
                "Choose a creature to put a counter on.");
    }
}
