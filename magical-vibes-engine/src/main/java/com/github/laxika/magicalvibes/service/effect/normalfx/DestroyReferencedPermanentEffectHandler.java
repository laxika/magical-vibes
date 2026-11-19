package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyReferencedPermanentEffect}: resolves the referenced permanent, then hands
 * it to {@link DestructionSupport#tryDestroyAndLog}, which owns indestructible, regeneration and
 * the death bookkeeping. A reference that no longer resolves is a silent no-op — none of the three
 * references targets, so there is nothing to fizzle.
 */
@Component
@RequiredArgsConstructor
public class DestroyReferencedPermanentEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyReferencedPermanentEffect e = (DestroyReferencedPermanentEffect) effect;

        Permanent referenced = subjectResolutionService.permanent(gameData, entry, effect, Subjects.of(e.reference()));
        if (referenced == null) {
            return;
        }

        if (e.reference() == PermanentReference.TRIGGERING) {
            // Later "that permanent's controller" instructions read the controller as it was destroyed.
            UUID controllerId = gameQueryService.findPermanentController(gameData, referenced.getId());
            if (controllerId != null) {
                entry.setTriggeringPermanentControllerId(controllerId);
            }
        }

        destructionSupport.destroyBatch(
                gameData, List.of(referenced), entry.getCard().getName(), e.cannotBeRegenerated());
    }
}
