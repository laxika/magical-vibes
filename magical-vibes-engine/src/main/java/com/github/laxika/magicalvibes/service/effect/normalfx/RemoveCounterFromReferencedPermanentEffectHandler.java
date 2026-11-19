package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromReferencedPermanentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/** Resolves counter removal from an Aura's attached permanent or another referenced permanent. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RemoveCounterFromReferencedPermanentEffect) effect;
        Permanent referenced = subjectResolutionService.permanent(
                gameData, entry, effect, Subjects.of(typedEffect.reference()));
        if (referenced == null) {
            return;
        }

        permanentCounterSupport.removeCounterFromPermanent(
                gameData, referenced, typedEffect.counterType(), typedEffect.amount());
    }
}
