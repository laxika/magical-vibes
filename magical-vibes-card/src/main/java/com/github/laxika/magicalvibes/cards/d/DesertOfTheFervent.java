package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "HOU", collectorNumber = "170")
@CardRegistration(set = "AKR", collectorNumber = "287")
public class DesertOfTheFervent extends Card {

    public DesertOfTheFervent() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // Cycling {1}{R} ({1}{R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{1}{R}");
    }
}
