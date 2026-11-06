package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "222")
public class TawnosTheToymaker extends Card {

    public TawnosTheToymaker() {
        var beastOrBirdCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.BEAST),
                        new CardSubtypePredicate(CardSubtype.BIRD)))));

        addTrigger(Triggers.youCastSpell().withFilter(beastOrBirdCreature),
                new MayEffect(new CopyTriggeringSpellEffect(true).withAdditionalTypes(Set.of(CardType.ARTIFACT)),
                        "Copy that creature spell?"));
    }
}
