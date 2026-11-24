package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithCardsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "DIS", collectorNumber = "46")
@CardRegistration(set = "RVR", collectorNumber = "78")
public class InfernalTutor extends Card {

    public InfernalTutor() {
        // Reveal a card from your hand. Search your library for a card with the same name as that card, reveal it,
        // put it into your hand, then shuffle.
        addSpellEffect(new ConditionalEffect(new NotCondition(new ControllerHandEmpty()), SequenceEffect.of(
                ChooseCardEffect.oneOf(Subjects.cardsInYourHand(new CardTruePredicate()),
                        "a card in your hand to reveal"),
                RevealCardsFromHandEffect.cards(Subjects.you(), Subjects.cardsThisWay(StepKind.CHOOSE)),
                new SearchLibraryEffect(new CardSharesNameWithCardsPredicate(Subjects.revealedThisWay()),
                        LibrarySearchDestination.HAND))));
        // Hellbent — If you have no cards in hand, instead search your library for a card, put it into your hand,
        // then shuffle. The hand is checked as the spell resolves; the search above puts a card into it.
        addSpellEffect(new ConditionalEffect(new ControllerHandEmpty(), new SearchLibraryEffect()));
    }
}
