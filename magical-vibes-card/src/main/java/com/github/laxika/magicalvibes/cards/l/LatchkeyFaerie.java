package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForProwlCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "39")
public class LatchkeyFaerie extends Card {

    public LatchkeyFaerie() {
        // Flying is auto-loaded from the Scryfall keyword registry.

        // Prowl {2}{U}: cast for this cost if you dealt combat damage to a player this turn
        // with a Faerie or Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{U}")),
                Set.of(CardSubtype.FAERIE, CardSubtype.ROGUE)));

        // When this creature enters, if its prowl cost was paid, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new CastForProwlCost(), new DrawCardEffect()));
    }
}
