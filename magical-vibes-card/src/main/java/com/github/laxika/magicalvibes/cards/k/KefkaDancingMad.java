package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "FIC", collectorNumber = "84")
@CardRegistration(set = "FIC", collectorNumber = "174")
public class KefkaDancingMad extends Card {

    public KefkaDancingMad() {
        // During your turn, Kefka has indestructible.
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new ControllerTurn(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        // At the beginning of your end step, exile a card at random from each opponent's graveyard.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffect());
    }
}
