package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "93")
public class LivelyDirge extends Card {

    public LivelyDirge() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{1}", "{2}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a card and put it into your graveyard",
                        new SearchLibraryEffect(null, LibrarySearchDestination.GRAVEYARD)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to two creature cards with total mana value 4 or less from your graveyard to the battlefield",
                        new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                                new CardTypePredicate(CardType.CREATURE), 2, 4))
        )));
    }
}
