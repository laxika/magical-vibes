package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "ZNR", collectorNumber = "135")
public class ArdentElectromancer extends Card {

    public ArdentElectromancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardManaEffect(ManaColor.RED, new PartySize()));
    }
}
