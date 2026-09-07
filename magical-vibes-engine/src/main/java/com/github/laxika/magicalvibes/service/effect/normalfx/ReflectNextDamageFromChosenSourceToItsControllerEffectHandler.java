package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectNextDamageFromChosenSourceToItsControllerEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReflectNextDamageFromChosenSourceToItsControllerEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReflectNextDamageFromChosenSourceToItsControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        List<UUID> validIds = preventionSupport.collectAllDamageSourceIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoDamageSourcesForChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ReflectDamageToSourceControllerChoice(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next time it would deal damage this turn, that damage is dealt "
                        + "to that source's controller instead.");
    }
}
