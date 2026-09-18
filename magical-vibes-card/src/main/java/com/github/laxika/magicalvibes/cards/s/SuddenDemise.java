package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenSpellColorPredicate;

@CardRegistration(set = "C13", collectorNumber = "124")
public class SuddenDemise extends Card {

    public SuddenDemise() {
        addEffect(EffectSlot.SPELL, new ChooseColorAtResolutionEffect());
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new XValue(), false, false,
                new PermanentHasChosenSpellColorPredicate()));
        addEffect(EffectSlot.SPELL, new ClearChosenColorEffect());
    }
}
