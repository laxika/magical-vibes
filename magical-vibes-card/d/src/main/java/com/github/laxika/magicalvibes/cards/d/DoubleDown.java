package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "OTJ", collectorNumber = "44")
public class DoubleDown extends Card {

    public DoubleDown() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ASSASSIN),
                        new CardSubtypePredicate(CardSubtype.MERCENARY),
                        new CardSubtypePredicate(CardSubtype.PIRATE),
                        new CardSubtypePredicate(CardSubtype.ROGUE),
                        new CardSubtypePredicate(CardSubtype.WARLOCK)))),
                new CopyTriggeringSpellEffect(true).withoutNewTargetChoice());
    }
}
