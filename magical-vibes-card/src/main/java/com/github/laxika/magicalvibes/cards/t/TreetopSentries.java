package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "BLB", collectorNumber = "201")
public class TreetopSentries extends Card {

    public TreetopSentries() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ForageEffect(new DrawCardEffect(1)),
                "Forage and draw a card?"));
    }
}
