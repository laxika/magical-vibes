package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "215")
public class YouMeetInATavern extends Card {

    public YouMeetInATavern() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Form a Party — Look at the top five cards of your library. You may reveal any number of creature cards from among them and put them into your hand. Put the rest on the bottom of your library in a random order.",
                        new LookAtTopCardsEffect(
                                new Fixed(5), new Fixed(5), new CardTypePredicate(CardType.CREATURE),
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                                LibrarySearchDestination.HAND, true)),
                new ChooseOneEffect.ChooseOneOption(
                        "Start a Brawl — Creatures you control get +2/+2 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 2))
        )));
    }
}
