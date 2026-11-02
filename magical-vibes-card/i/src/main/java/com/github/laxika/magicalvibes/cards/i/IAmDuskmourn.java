package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "334")
public class IAmDuskmourn extends Card {

    public IAmDuskmourn() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        null, null, new SacrificeSelfEffect()));
    }
}
