package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "81")
public class Triskaidekaphile extends Card {

    public Triskaidekaphile() {
        addEffect(EffectSlot.STATIC, new NoMaximumHandSizeEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(new CardsInHandAtLeast(13), new CardsInHandAtMost(13))),
                new WinGameEffect()));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new DrawCardEffect(1)),
                "{3}{U}: Draw a card."
        ));
    }
}
