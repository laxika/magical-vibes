package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "HOU", collectorNumber = "174")
@CardRegistration(set = "AKR", collectorNumber = "291")
public class DesertOfTheTrue extends Card {

    public DesertOfTheTrue() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        // Cycling {1}{W} ({1}{W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{1}{W}");
    }
}
