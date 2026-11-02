package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "224")
public class DiamondKnight extends Card {

    public DiamondKnight() {
        addStaticEffect(new ChooseColorOnEnterEffect());
        addTrigger(Triggers.youCastSpell(),
                new PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect(1));
    }
}
