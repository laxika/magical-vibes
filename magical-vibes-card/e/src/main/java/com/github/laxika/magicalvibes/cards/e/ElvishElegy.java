package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "19")
public class ElvishElegy extends Card {

    public ElvishElegy() {
        addSpellEffect(SequenceEffect.of(
                new MillEffect(3, Subjects.you()),
                new PerpetuallyBoostCreatureCardsInGraveyardEffect(1, 1),
                ChooseCardEffect.upTo(
                        Subjects.cardsThisWay(StepKind.MILL, new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.ELF),
                                new CardTypePredicate(CardType.LAND)))), 1,
                        "an Elf or land card milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
