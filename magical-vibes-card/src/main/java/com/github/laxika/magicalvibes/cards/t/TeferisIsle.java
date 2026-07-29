package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "330")
public class TeferisIsle extends Card {

    public TeferisIsle() {
        // Phasing is a printed keyword loaded from Scryfall; PhasingService handles it.

        // Teferi's Isle enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {U}{U}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.BLUE, 2)),
                "{T}: Add {U}{U}."
        ));
    }
}
