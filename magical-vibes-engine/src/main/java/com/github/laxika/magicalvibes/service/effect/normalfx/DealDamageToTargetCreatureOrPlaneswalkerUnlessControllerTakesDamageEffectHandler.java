package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted permanent's damage-or-controller-damage choice. */
@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect) effect;
        UUID targetId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (targetControllerId == null) {
            return;
        }

        String sourceName = entry.getCard().getName();
        String prompt = "Have " + sourceName + " deal " + e.controllerDamage()
                + " damage to you? If you don't, " + sourceName + " deals "
                + e.targetDamage() + " damage to " + target.getCard().getName() + ". ("
                + sourceName + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetControllerId, List.of(e), prompt,
                targetId, null, entry.getSourcePermanentId(), null, 0, 0,
                null, null, null, entry.getSourcePermanentSnapshot(), entry.getControllerId(), null, 0));
    }
}
