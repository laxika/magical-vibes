package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetFromChosenSourceEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventDamageToTargetFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageToTargetFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageToTargetFromChosenSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);

        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PreventDamageToTargetFromSourceChoice(controllerId, e.amount(), targetId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next " + e.amount() + " damage it would deal to the target is prevented.");
    }
}
