package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "44")
public class TazriBeaconOfUnity extends Card {

    public TazriBeaconOfUnity() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PartySize()));

        addActivatedAbility(new ActivatedAbility(false, "{2/U}{2/B}{2/R}{2/G}", List.of(
                new LookAtTopCardsEffect(
                        new Fixed(6),
                        new Fixed(2),
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.CLERIC),
                                new CardSubtypePredicate(CardSubtype.ROGUE),
                                new CardSubtypePredicate(CardSubtype.WARRIOR),
                                new CardSubtypePredicate(CardSubtype.WIZARD),
                                new CardSubtypePredicate(CardSubtype.ALLY))),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                        false,
                        LibrarySearchDestination.HAND,
                        true)),
                "{2/U}{2/B}{2/R}{2/G}: Look at the top six cards of your library. You may reveal up to two Cleric, Rogue, Warrior, Wizard, and/or Ally cards from among them and put them into your hand. Put the rest on the bottom of your library in a random order."));
    }
}
