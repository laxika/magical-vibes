package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves My Laughter Echoes' abandon-and-repeat instruction. */
@Component
@RequiredArgsConstructor
public class AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffectHandler
        implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameQueryService gameQueryService;
    private final SchemeAbandonSupport schemeAbandonSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry schemeSnapshot = entry.getTriggeringSpellSnapshot();
        if (schemeSnapshot == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null
                && !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, source.getId()))) {
            return;
        }
        if (!schemeAbandonSupport.abandon(gameData, entry.getCard(), entry.getSourcePermanentId())) {
            return;
        }

        // Setting the scheme in motion again triggers its "when you set this scheme in motion" ability again
        // (CR 701.32b): a new ability, whose targets are chosen anew as it goes on the stack (CR 603.3d).
        StackEntry replay = TriggeredAbilityStackService.madeAbility(
                schemeSnapshot.getCard(),
                entry.getControllerId(),
                schemeSnapshot.getDescription(),
                schemeSnapshot.getEffectsToResolve(),
                (UUID) null,
                schemeSnapshot.getSourcePermanentId());
        triggeredAbilityStackService.putMade(gameData, replay);
        triggerCollectionService.checkSchemeSetInMotionTriggers(gameData, replay);
    }
}
