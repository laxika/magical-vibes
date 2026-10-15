package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "9")
public class KaylasCommand extends Card {

    public KaylasCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 2/2 colorless Construct artifact creature token",
                        new CreateTokenEffect("Construct", 2, 2, null,
                                List.of(CardSubtype.CONSTRUCT), Set.of(), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on a creature you control. It gains double strike until end of turn",
                        List.of(
                                new PutCounterOnChosenOwnPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentIsCreaturePredicate()),
                                new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.DOUBLE_STRIKE, null)
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a basic Plains card, reveal it, put it into your hand, then shuffle",
                        new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardSubtypePredicate(CardSubtype.PLAINS)
                        )), LibrarySearchDestination.HAND)),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 2 life and scry 2",
                        List.of(new GainLifeEffect(2), new ScryEffect(2)))
        ), 2));
    }
}
