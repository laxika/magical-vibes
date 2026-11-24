package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "APC", collectorNumber = "123")
public class SqueesRevenge extends Card {

    public SqueesRevenge() {
        addSpellEffect(new ChooseNumberAtResolutionEffect(Integer.MAX_VALUE));
        addSpellEffect(FlipUntilLoseEffect.upTo(
                new XValue(), new DrawCardEffect(new Scaled(new XValue(), 2))));
    }
}
