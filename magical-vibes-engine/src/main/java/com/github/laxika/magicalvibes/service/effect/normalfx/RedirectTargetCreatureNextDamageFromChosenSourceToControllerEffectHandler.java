package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectTargetCreatureNextDamageFromChosenSourceToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID protectedCreatureId = entry.getTargetId();
        // Without a target creature the ability does nothing. The redirected damage goes to the controller.
        if (protectedCreatureId == null || controllerId == null) return;

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectCreatureDamageSourceChoice(controllerId, protectedCreatureId, controllerId, true));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next time it would deal damage to the target creature this turn, that damage is dealt to you instead.");
    }
}
