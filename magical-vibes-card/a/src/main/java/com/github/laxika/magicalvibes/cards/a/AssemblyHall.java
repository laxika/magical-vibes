package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithCardsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "286")
public class AssemblyHall extends Card {

    public AssemblyHall() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        ChooseCardEffect.oneOf(Subjects.cardsInYourHand(new CardTypePredicate(CardType.CREATURE)),
                                "a creature card in your hand to reveal"),
                        RevealCardsFromHandEffect.cards(Subjects.you(), Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new SearchLibraryEffect(new CardSharesNameWithCardsPredicate(Subjects.revealedThisWay()),
                                LibrarySearchDestination.HAND)),
                "{4}, {T}: Reveal a creature card in your hand. Search your library for a card with the same name as that card, reveal it, put it into your hand, then shuffle."
        ));
    }
}
