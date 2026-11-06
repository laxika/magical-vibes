package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "2XM", collectorNumber = "214")
@CardRegistration(set = "CMD", collectorNumber = "220")
public class RikuOfTwoReflections extends Card {

    public RikuOfTwoReflections() {
        // Whenever you cast an instant or sorcery spell, you may pay {U}{R}. If you do, copy that
        // spell. You may choose new targets for the copy.
        addTrigger(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new MayPayManaEffect("{U}{R}", new CopyTriggeringSpellEffect(), "Pay {U}{R} to copy that spell?"));

        // Whenever another nontoken creature you control enters, you may pay {G}{U}. If you do,
        // create a token that's a copy of that creature.
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayPayManaEffect(
                "{G}{U}",
                new CreateTokenCopyOfTargetPermanentEffect(),
                "Pay {G}{U} to create a token that's a copy of that creature?"
        ));
    }
}
