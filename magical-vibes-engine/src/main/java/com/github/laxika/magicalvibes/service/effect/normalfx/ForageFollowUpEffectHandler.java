package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForageFollowUpEffect;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted forage rider after the forage action has succeeded. */
@Component
@RequiredArgsConstructor
public class ForageFollowUpEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForageFollowUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ForageFollowUpEffect followUp = (ForageFollowUpEffect) effect;
        GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(
                List.of(followUp.thenEffect()));
        if (target == null || target.scope() != GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
            return;
        }

        // "When you do" is an ability of its own (CR 603.12): its graveyard target is chosen as it goes on the stack,
        // and it records what the resolving entry was about.
        triggeredAbilityStackService.putMade(gameData, ReflexiveAbilitySupport.record(gameData, entry,
                List.of(followUp.thenEffect()), entry.getCard().getName() + "'s ability"));
    }
}
