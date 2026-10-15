package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "16")
public class MarduDevotee extends Card {

    public MarduDevotee() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.RED, ManaColor.WHITE, ManaColor.BLACK))),
                "{1}: Add {R}, {W}, or {B}. Activate only once each turn.",
                1
        ));
    }
}
