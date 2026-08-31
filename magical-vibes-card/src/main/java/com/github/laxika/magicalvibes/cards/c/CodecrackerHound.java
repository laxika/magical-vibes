package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "50")
public class CodecrackerHound extends Card {

    public CodecrackerHound() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseNToHandRestToGraveyard(2, 1));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}"))));
    }
}
