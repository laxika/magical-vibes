package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenNameSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChosenNameSpellCastTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChosenNameSpellCastTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.getChosenName() == null) {
            return;
        }

        var trigger = (ChosenNameSpellCastTriggerEffect) effect;
        gameData.queueDelayedAction(new DelayedControllerSpellCastTrigger(
                entry.getControllerId(), entry.getSourcePermanentId(), entry.getCard(),
                new CardNamedPredicate(source.getChosenName()), trigger.resolvedEffects(), true, false));
        log.info("Game {} - {} registers a first chosen-name spell-cast trigger for this turn",
                gameData.id, entry.getCard().getName());
    }
}
