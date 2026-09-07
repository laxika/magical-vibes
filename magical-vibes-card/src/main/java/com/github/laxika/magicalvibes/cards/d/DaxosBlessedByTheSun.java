package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "THB", collectorNumber = "9")
public class DaxosBlessedByTheSun extends Card {

    public DaxosBlessedByTheSun() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new Fixed(2), new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new GainLifeEffect(1));
    }
}
