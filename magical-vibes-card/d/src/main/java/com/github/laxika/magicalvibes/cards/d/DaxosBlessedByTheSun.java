package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "9")
@CardRegistration(set = "THB", collectorNumber = "258")
@CardRegistration(set = "MUL", collectorNumber = "2")
@CardRegistration(set = "MUL", collectorNumber = "67")
@CardRegistration(set = "MUL", collectorNumber = "132")
public class DaxosBlessedByTheSun extends Card {

    public DaxosBlessedByTheSun() {
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new Fixed(2), new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE)));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new GainLifeEffect(1));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new GainLifeEffect(1));
    }
}
