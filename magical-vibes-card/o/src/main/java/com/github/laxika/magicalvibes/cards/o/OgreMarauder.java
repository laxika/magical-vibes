package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "75")
public class OgreMarauder extends Card {

    public OgreMarauder() {
        // Whenever this creature attacks, it gains "this creature can't be blocked" until end of
        // turn unless defending player sacrifices a creature of their choice.
        addTrigger(Triggers.attacks(), UnlessPaysEffect.unlessPays(Subjects.defendingPlayer(),
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                new GrantEffectToTargetUntilEndOfTurnEffect(StaticAbility.of(new CantBeBlockedEffect()),
                        Subjects.source())));
    }
}
