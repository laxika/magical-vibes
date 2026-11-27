package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectNextDamageFromChosenSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final DamageSourceChoiceSupport damageSourceChoiceSupport;
    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageFromChosenSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        PermanentSubject subject = ((RedirectNextDamageFromChosenSourceToTargetCreatureEffect) effect).subject();
        List<UUID> redirectIds = subjectResolutionService.permanentIds(gameData, entry, effect, subject);
        if (controllerId == null || redirectIds.isEmpty()) return;
        UUID redirectTargetId = redirectIds.getFirst();

        List<UUID> validIds = damageSourceChoiceSupport.choosable(gameData, entry);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoDamageSourcesForChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectPlayerDamageSourceChoice(controllerId, redirectTargetId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next time it would deal damage to you this turn, "
                        + "that damage is dealt to "
                        + (subject.declaresTarget() ? "the target" : "this") + " creature instead.");
    }
}
