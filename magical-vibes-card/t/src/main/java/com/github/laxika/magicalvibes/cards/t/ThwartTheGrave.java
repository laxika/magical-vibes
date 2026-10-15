package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "130")
public class ThwartTheGrave extends Card {

    public ThwartTheGrave() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PartySize()));

        CardPredicate creature = new CardTypePredicate(CardType.CREATURE);
        CardPredicate partyCreature = new CardAllOfPredicate(List.of(
                creature,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.CLERIC),
                        new CardSubtypePredicate(CardSubtype.ROGUE),
                        new CardSubtypePredicate(CardSubtype.WARRIOR),
                        new CardSubtypePredicate(CardSubtype.WIZARD)))));
        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                List.of(creature, partyCreature),
                List.of(GraveyardChoiceDestination.BATTLEFIELD, GraveyardChoiceDestination.BATTLEFIELD),
                List.of("creature card", "Cleric, Rogue, Warrior, or Wizard creature card"),
                List.of(1, 0),
                false));
    }
}
