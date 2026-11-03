package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceChosenNumberPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "8")
public class OminousLockbox extends Card {

    public OminousLockbox() {
        addStaticEffect(new ChooseNumberOnEnterEffect(1, 20));
        addTrigger(Triggers.opponentCastsSpell()
                        .withStackEntryFilter(new StackEntryManaValueEqualsSourceChosenNumberPredicate()),
                new SacrificeSelfThenEffect(new CopyTriggeringSpellEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice Ominous Lockbox: Draw a card."
        ));
    }
}
