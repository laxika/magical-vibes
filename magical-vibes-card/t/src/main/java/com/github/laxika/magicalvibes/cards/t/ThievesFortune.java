package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "54")
public class ThievesFortune extends Card {

    public ThievesFortune() {
        // Prowl {U}: cast for this cost if you dealt combat damage to a player this turn with a Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{U}")), CardSubtype.ROGUE));

        // Look at the top four cards of your library. Put one of them into your hand and the rest
        // on the bottom of your library in any order.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(4)));
    }
}
