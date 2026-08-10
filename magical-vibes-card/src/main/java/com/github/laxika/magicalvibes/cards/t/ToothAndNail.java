package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "134")
public class ToothAndNail extends Card {

    public ToothAndNail() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{2}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for up to two creature cards, reveal them, and put them into your hand",
                        new SearchLibraryEffect(new Fixed(2), creature, LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put up to two creature cards from your hand onto the battlefield",
                        List.of(
                                new MayEffect(new PutCardToBattlefieldEffect(creature, "creature"),
                                        "Put a creature card from your hand onto the battlefield?"),
                                new MayEffect(new PutCardToBattlefieldEffect(creature, "creature"),
                                        "Put a second creature card from your hand onto the battlefield?"))
                )
        )));
    }
}
