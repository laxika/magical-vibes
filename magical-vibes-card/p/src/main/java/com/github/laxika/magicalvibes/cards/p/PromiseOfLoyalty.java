package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect;

@CardRegistration(set = "FIC", collectorNumber = "249")
@CardRegistration(set = "SOC", collectorNumber = "161")
@CardRegistration(set = "MSC", collectorNumber = "142")
@CardRegistration(set = "MSC", collectorNumber = "319")
@CardRegistration(set = "MOC", collectorNumber = "200")
@CardRegistration(set = "C21", collectorNumber = "21")
@CardRegistration(set = "MKC", collectorNumber = "79")
@CardRegistration(set = "BLC", collectorNumber = "148")
public class PromiseOfLoyalty extends Card {

    public PromiseOfLoyalty() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect());
    }
}
