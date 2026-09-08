package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "160")
public class DanceOfTheTumbleweeds extends Card {

    public DanceOfTheTumbleweeds() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{3}")));

        CardAnyOfPredicate basicLandOrDesert = new CardAnyOfPredicate(List.of(
                CardPredicateUtils.basicLand(),
                new CardSubtypePredicate(CardSubtype.DESERT)));
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a basic land card or a Desert card, put it onto the battlefield",
                        new SearchLibraryEffect(basicLandOrDesert, LibrarySearchDestination.BATTLEFIELD)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create an X/X green Elemental creature token, where X is the number of lands you control",
                        new CreateTokenEffect("Elemental", landsYouControl, landsYouControl,
                                CardColor.GREEN, List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of()))
        )));
    }
}
