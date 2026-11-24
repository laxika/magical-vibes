package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * King Narfi's Betrayal — {1}{U}{B} Enchantment — Saga
 *
 * I — Each player mills four cards. Then you may exile a creature or planeswalker card from each
 * graveyard.
 * II, III — Until end of turn, you may cast spells from among cards exiled with this Saga, and you
 * may spend mana as though it were mana of any color to cast those spells.
 */
@CardRegistration(set = "KHM", collectorNumber = "219")
@CardRegistration(set = "LCC", collectorNumber = "272")
public class KingNarfisBetrayal extends Card {

    public KingNarfisBetrayal() {
        addTrigger(Triggers.chapter(1),
                new MillEffect(4, Subjects.eachPlayer()),
                ChooseCardEffect.upTo(Subjects.cardsInGraveyards(Subjects.eachPlayer(),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER)
                        ))), 1, "a creature or planeswalker card to exile").fromEachOwner(),
                ExileGraveyardCardsEffect.cardsWithSource(Subjects.cardsThisWay(StepKind.CHOOSE)));

        var nonlandCard = new CardNotPredicate(new CardTypePredicate(CardType.LAND));
        var castSpellsFromAmongExiled = AllowPlayCardsFromExileEffect.thisTurn(
                Subjects.exiledWithSourceMatching(nonlandCard)).withAnyManaType().alsoCardsExiledLaterThisTurn();
        addTrigger(Triggers.chapter(2), castSpellsFromAmongExiled);
        addTrigger(Triggers.chapter(3), castSpellsFromAmongExiled);
    }
}
