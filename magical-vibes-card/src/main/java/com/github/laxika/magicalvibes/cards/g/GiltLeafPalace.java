package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;


@CardRegistration(set = "LRW", collectorNumber = "268")
public class GiltLeafPalace extends Card {

    public GiltLeafPalace() {
        // As this land enters, you may reveal an Elf card from your hand.
        // If you don't, this land enters tapped.
        addEffect(EffectSlot.STATIC, new RevealSubtypeOrEntersTappedEffect(CardSubtype.ELF));

        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
