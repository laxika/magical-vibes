package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "HOU", collectorNumber = "135")
public class SifterWurm extends Card {

    public SifterWurm() {
        // Trample is auto-loaded from Scryfall.
        // When this creature enters, scry 3, then reveal the top card of your library.
        // You gain life equal to that card's mana value.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardGainLifeEqualToManaValueEffect());
    }
}
