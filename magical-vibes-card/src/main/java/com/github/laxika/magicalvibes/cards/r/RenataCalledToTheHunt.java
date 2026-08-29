package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "THB", collectorNumber = "196")
public class RenataCalledToTheHunt extends Card {

    public RenataCalledToTheHunt() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.GREEN), new Fixed(3)));
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersEffect(
                new PermanentIsCreaturePredicate(), 1));
    }
}
