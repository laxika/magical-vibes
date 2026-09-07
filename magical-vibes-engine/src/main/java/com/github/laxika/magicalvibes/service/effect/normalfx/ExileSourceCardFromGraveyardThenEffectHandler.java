package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileSourceCardFromGraveyardThenEffectHandler implements NormalEffectHandlerBean {

    private final ExileSourceCardFromGraveyardEffectHandler exileHandler;
    private final GameQueryService gameQueryService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromGraveyardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileSourceCardFromGraveyardThenEffect) effect;
        if (gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId()) == null) {
            return;
        }

        exileHandler.resolve(gameData, entry, new ExileSourceCardFromGraveyardEffect());

        if (gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId()) != null) {
            return;
        }

        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                entry.getCard(), entry.getControllerId(), List.of(exileThen.thenEffect()),
                null, 0, 0, 0, entry.getTriggeringPermanentPowerAtTrigger()));
        triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
    }
}
