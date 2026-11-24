package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "21")
public class KithkinBrinefarer extends Card {

    public KithkinBrinefarer() {
        addTrigger(Triggers.putIntoHandFrom(
                        Zone.LIBRARY,
                        new PermanentHasSubtypePredicate(CardSubtype.KITHKIN),
                        ObjectRelation.YOU_OWN),
                ConjureCardsEffect.duplicateOf(Subjects.triggeringCardsInHand(), ConjureDestination.intoHand()));
        addTrigger(Triggers.youAttack(),
                new PerpetuallyBoostMatchingHandCardsEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.KITHKIN),
                                new CardTypePredicate(CardType.CREATURE))), 1, 1));
    }
}
