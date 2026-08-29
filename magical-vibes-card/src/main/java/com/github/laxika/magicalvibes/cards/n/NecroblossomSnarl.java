package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "269")
public class NecroblossomSnarl extends Card {

    public NecroblossomSnarl() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.SWAMP, CardSubtype.FOREST)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
