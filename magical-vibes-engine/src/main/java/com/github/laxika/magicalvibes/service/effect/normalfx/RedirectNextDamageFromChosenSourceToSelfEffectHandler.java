package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectNextDamageFromChosenSourceToSelfEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageFromChosenSourceToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID destinationId = entry.getSourcePermanentId();
        if (destinationId == null) {
            return;
        }

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectNextDamageFromChosenSourceToPermanentChoice(
                        controllerId, destinationId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next time it would deal damage this turn, that damage is dealt to "
                        + entry.getCard().getName() + " instead.");
    }
}
