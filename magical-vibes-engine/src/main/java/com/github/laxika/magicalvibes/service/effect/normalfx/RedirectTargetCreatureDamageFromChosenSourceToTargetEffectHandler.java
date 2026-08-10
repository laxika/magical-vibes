package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToTargetEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectTargetCreatureDamageFromChosenSourceToTargetEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectTargetCreatureDamageFromChosenSourceToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var redirect = (RedirectTargetCreatureDamageFromChosenSourceToTargetEffect) effect;
        List<UUID> protectedTargets = entry.targetsForGroup(redirect.protectedTargetGroup());
        List<UUID> redirectTargets = entry.targetsForGroup(redirect.redirectTargetGroup());
        if (protectedTargets.isEmpty() || redirectTargets.isEmpty()) return;

        List<UUID> validIds = preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectCreatureDamageSourceChoice(
                        entry.getControllerId(), protectedTargets.getFirst(), redirectTargets.getFirst(), false));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validIds,
                "Choose a source. All damage it would deal to the target creature this turn is dealt to another target creature instead.");
    }
}
