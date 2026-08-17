package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasNoAbilitiesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "178")
public class RiseFromTheWreck extends Card {

    public RiseFromTheWreck() {
        CardPredicate creature = new CardTypePredicate(CardType.CREATURE);
        CardPredicate creatureWithNoAbilities = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardHasNoAbilitiesPredicate()));

        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToHandEffect(List.of(
                creature,
                new CardSubtypePredicate(CardSubtype.MOUNT),
                new CardSubtypePredicate(CardSubtype.VEHICLE),
                creatureWithNoAbilities)));
    }
}
