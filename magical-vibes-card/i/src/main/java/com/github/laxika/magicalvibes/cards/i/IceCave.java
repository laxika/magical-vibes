package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "24")
public class IceCave extends Card {

    public IceCave() {
        // Whenever a player casts a spell, any other player may pay that spell's mana cost. If a player does, counter
        // the spell.
        addTrigger(Triggers.playerCastsSpell().anySpell(), new UnlessPaysEffect(
                Subjects.except(Subjects.eachPlayer(), Subjects.controllerOf(Subjects.triggeringSpell())),
                PayManaCost.ofTriggeringSpell(), null, CounterSpellEffect.triggeringSpell()));
    }
}
