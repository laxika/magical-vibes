package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectTargetCreatureDamageFromChosenSourceToSelfEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectTargetCreatureDamageFromChosenSourceToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID protectedCreatureId = entry.getTargetId();
        UUID redirectTargetId = entry.getSourcePermanentId();
        // Without a target creature or a source permanent to redirect onto, the ability does nothing.
        if (protectedCreatureId == null || redirectTargetId == null) return;

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectCreatureDamageSourceChoice(controllerId, protectedCreatureId, redirectTargetId, false));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. All damage it would deal to the target creature this turn is redirected instead.");
    }
}
