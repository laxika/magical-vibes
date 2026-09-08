package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "192")
public class KinnanBonderProdigy extends Card {

    public KinnanBonderProdigy() {
        addEffect(EffectSlot.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA,
                new AddManaOfTypeProducedByTappedPermanentEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}{U}",
                List.of(LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        5,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN)))))),
                "{5}{G}{U}: Look at the top five cards of your library. You may put a non-Human creature "
                        + "card from among them onto the battlefield. Put the rest on the bottom of your "
                        + "library in a random order."
        ));
    }
}
