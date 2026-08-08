package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
    private final AmountEvaluationService amountEvaluationService;

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

        int amount = amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, null));
        if (amount <= 0) return;

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);

        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectDamageSourceChoice(controllerId, amount, redirectTargetId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next " + amount + " damage it would deal to you or your permanents is redirected.");
    }
}
