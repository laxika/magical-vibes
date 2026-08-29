package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "278")
public class PortTown extends Card {

    public PortTown() {
        addEffect(EffectSlot.STATIC,
                new RevealSubtypeOrEntersTappedEffect(Set.of(CardSubtype.PLAINS, CardSubtype.ISLAND)));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
