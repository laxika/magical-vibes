package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.LandPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "35")
@CardRegistration(set = "DMC", collectorNumber = "57")
public class TheLadyOfOtaria extends Card {

    public TheLadyOfOtaria() {
        addCastingOption(new AlternateHandCast(List.of(
                new TapUntappedPermanentsCost(3, new PermanentHasSubtypePredicate(CardSubtype.DWARF)))));

        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new LandPutIntoGraveyardFromBattlefieldThisTurn()),
                new LookAtTopCardsEffect(new Fixed(4), new Fixed(4),
                        new CardSubtypePredicate(CardSubtype.DWARF),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true,
                        LibrarySearchDestination.HAND, true));
    }
}
