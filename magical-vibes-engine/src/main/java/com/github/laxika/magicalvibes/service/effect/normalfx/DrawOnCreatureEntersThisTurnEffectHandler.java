package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawOnCreatureEntersThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "Whenever a creature enters this turn, you may draw a card" (Beck): a delayed triggered ability with a stated
 * duration, the spell's and controlled by its controller (CR 603.7b, 603.7e), that triggers for every creature that
 * enters, whoever controls it. Each registration is its own ability.
 */
@Slf4j
@Component
public class DrawOnCreatureEntersThisTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawOnCreatureEntersThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.thisTurn(
                entry.getControllerId(), entry.getCard(),
                new TriggeredAbility(Triggers.enters(new PermanentIsCreaturePredicate(), ObjectRelation.ANY),
                        new MayEffect(new DrawCardEffect(), "Draw a card?")),
                entry.getSourcePermanentId()));
        log.info("Game {} - {} may draw a card whenever a creature enters this turn",
                gameData.id, entry.getControllerId());
    }
}
