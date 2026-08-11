package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect;

@CardRegistration(set = "ODY", collectorNumber = "257")
public class NewFrontiers extends Card {

    public NewFrontiers() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect(new XValue(), true));
    }
}
