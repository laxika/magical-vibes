package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect;

@CardRegistration(set = "ORI", collectorNumber = "38")
public class TragicArrogance extends Card {

    public TragicArrogance() {
        // For each player, you choose an artifact, a creature, an enchantment and a planeswalker
        // that player controls; then each player sacrifices all other nonland permanents.
        addEffect(EffectSlot.SPELL, new ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect());
    }
}
