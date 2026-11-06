package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ARB", collectorNumber = "86")
public class ClovenCasting extends Card {

    public ClovenCasting() {
        // Whenever you cast a multicolored instant or sorcery spell, you may pay {1}. If you do,
        // copy that spell. You may choose new targets for the copy.
        addTrigger(Triggers.youCastSpell().withFilter(new CardAllOfPredicate(List.of(
                        new CardIsMulticoloredPredicate(),
                        new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))))),
                new MayPayManaEffect("{1}", new CopyTriggeringSpellEffect(), "Pay {1} to copy that spell?"));
    }
}
