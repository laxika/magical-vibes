package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventDamageFromChosenSourceAndRedirectToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID redirectTargetId = entry.getTargetId();
        if (redirectTargetId == null) return;

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);

        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectDamageSourceChoice(controllerId, e.amount(), redirectTargetId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next " + e.amount() + " damage it would deal to you or your permanents is redirected.");
    }
}
