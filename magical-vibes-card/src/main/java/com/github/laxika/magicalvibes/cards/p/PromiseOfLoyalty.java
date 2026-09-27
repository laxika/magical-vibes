package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreatureThenSacrificesRestEffect;

@CardRegistration(set = "FIC", collectorNumber = "249")
public class PromiseOfLoyalty extends Card {

    public PromiseOfLoyalty() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCreatureThenSacrificesRestEffect(CounterType.VOW));
    }
}
