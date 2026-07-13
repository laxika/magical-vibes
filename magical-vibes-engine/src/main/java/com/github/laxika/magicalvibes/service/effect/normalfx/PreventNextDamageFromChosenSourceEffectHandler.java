package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreventNextDamageFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        boolean gainLife = ((PreventNextDamageFromChosenSourceEffect) effect).gainLife();
        UUID controllerId = entry.getControllerId();

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PreventNextDamageFromSourceChoice(controllerId, gainLife));
        String prompt = gainLife
                ? "Choose a source. The next time it would deal damage to you this turn, prevent that damage and gain that much life."
                : "Choose a source. The next time it would deal damage to you this turn, prevent that damage.";
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, prompt);
    }
}
