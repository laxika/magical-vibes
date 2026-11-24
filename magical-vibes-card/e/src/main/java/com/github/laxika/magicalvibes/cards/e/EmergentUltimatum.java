package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CastCardsWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyNColorsPredicate;

@CardRegistration(set = "IKO", collectorNumber = "185")
@CardRegistration(set = "SPG", collectorNumber = "105")
public class EmergentUltimatum extends Card {

    public EmergentUltimatum() {
        addSpellEffect(new SearchLibraryEffect(new Fixed(3), new CardHasExactlyNColorsPredicate(1),
                LibrarySearchDestination.EXILE, null, true).withoutShuffle());
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.cardsThisWay(StepKind.SEARCH), "one of those cards")
                .by(Subjects.playersThisWay(StepKind.CHOOSE)));
        addSpellEffect(PutCardsIntoLibraryEffect.shuffledIn(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(CastCardsWithoutPayingManaCostEffect.anyNumber(Subjects.cardsThisWay(StepKind.SEARCH)));
        addSpellEffect(new ExileSpellEffect());
    }
}
