package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ARB", collectorNumber = "113")
public class Thraximundar extends Card {

    public Thraximundar() {
        // Haste is auto-loaded from Scryfall as a keyword.

        // Whenever Thraximundar attacks, defending player sacrifices a creature of their choice.
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        // Whenever a player sacrifices a creature, you may put a +1/+1 counter on Thraximundar.
        addEffect(EffectSlot.ON_ANY_CREATURE_SACRIFICED, new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Thraximundar?"));
    }
}
