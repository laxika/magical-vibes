package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ODY", collectorNumber = "303")
@CardRegistration(set = "TSB", collectorNumber = "112")
@CardRegistration(set = "V10", collectorNumber = "9")
@CardRegistration(set = "C13", collectorNumber = "246")
public class Mirari extends Card {

    public Mirari() {
        // Whenever you cast an instant or sorcery spell, you may pay {3}.
        // If you do, copy that spell. You may choose new targets for the copy.
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new MayPayManaEffect("{3}", new CopyTriggeringSpellEffect(), "Pay {3} to copy that spell?"));
    }
}
