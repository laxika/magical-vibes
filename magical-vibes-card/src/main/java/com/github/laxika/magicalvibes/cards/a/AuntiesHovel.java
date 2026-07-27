package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;


@CardRegistration(set = "LRW", collectorNumber = "267")
public class AuntiesHovel extends Card {

    public AuntiesHovel() {
        // As this land enters, you may reveal a Goblin card from your hand.
        // If you don't, this land enters tapped.
        addEffect(EffectSlot.STATIC, new RevealSubtypeOrEntersTappedEffect(CardSubtype.GOBLIN));

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
