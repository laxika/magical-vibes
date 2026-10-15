package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.LandPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "35")
@CardRegistration(set = "DMC", collectorNumber = "57")
public class TheLadyOfOtaria extends Card {

    public TheLadyOfOtaria() {
        addCastingOption(new AlternateHandCast(List.of(
                new TapUntappedPermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.DWARF)))));

        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new LandPutIntoGraveyardFromBattlefieldThisTurn(),
                new LookAtTopCardsEffect(new Fixed(4), new Fixed(4),
                        new CardSubtypePredicate(CardSubtype.DWARF),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true,
                        LibrarySearchDestination.HAND, true)));
    }
}
