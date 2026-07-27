package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "243")
public class FetidPools extends Card {

    public FetidPools() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new DrawCardEffect(1)),
                "Cycling {2} ({2}, Discard this card: Draw a card.)"));
    }
}
