package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;


@CardRegistration(set = "HOU", collectorNumber = "171")
@CardRegistration(set = "AKR", collectorNumber = "288")
public class DesertOfTheGlorified extends Card {

    public DesertOfTheGlorified() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        // Cycling {1}{B} ({1}{B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{1}{B}");
    }
}
