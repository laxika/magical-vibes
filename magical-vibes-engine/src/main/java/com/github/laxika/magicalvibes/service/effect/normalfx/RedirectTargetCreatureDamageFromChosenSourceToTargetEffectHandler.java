package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToTargetEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectTargetCreatureDamageFromChosenSourceToTargetEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectTargetCreatureDamageFromChosenSourceToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var redirect = (RedirectTargetCreatureDamageFromChosenSourceToTargetEffect) effect;
        // Ids, not live permanents: the shield names the creatures, and redirection checks them as damage would be dealt.
        List<UUID> protectedIds = subjectResolutionService.permanentIds(gameData, entry, effect,
                redirect.protectedCreature());
        List<UUID> redirectIds = subjectResolutionService.permanentIds(gameData, entry, effect, redirect.redirectTo());
        if (protectedIds.isEmpty() || redirectIds.isEmpty()) return;

        List<UUID> validIds = preventionSupport.collectAllDamageSourceIds(gameData);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoDamageSourcesForChoice(gameData);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectCreatureDamageSourceChoice(
                        entry.getControllerId(), protectedIds.getFirst(), redirectIds.getFirst(), false));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validIds,
                "Choose a source. All damage it would deal to the target creature this turn is dealt to "
                        + (redirect.redirectTo().declaresTarget() ? "another target creature" : entry.getCard().getName())
                        + " instead.");
    }
}
