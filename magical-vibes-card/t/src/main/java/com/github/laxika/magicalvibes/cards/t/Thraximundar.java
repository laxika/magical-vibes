package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "113")
@CardRegistration(set = "SLD", collectorNumber = "142")
@CardRegistration(set = "2X2", collectorNumber = "287")
@CardRegistration(set = "C13", collectorNumber = "221")
public class Thraximundar extends Card {

    public Thraximundar() {
        // Haste is auto-loaded from Scryfall as a keyword.

        // Whenever Thraximundar attacks, defending player sacrifices a creature of their choice.
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        // Whenever a player sacrifices a creature, you may put a +1/+1 counter on Thraximundar.
        addTrigger(Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.ANY), new MayEffect(
                new PutCountersOnSourceEffect(1, 1, 1),
                "Put a +1/+1 counter on Thraximundar?"));
    }
}
