package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "17")
public class ConsumingOni extends Card {

    public ConsumingOni() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect(
                        EffectSlot.ON_SELF_CAST,
                        List.of(new LoseLifeEffect(3)),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
