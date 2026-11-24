package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "289")
public class CreditVoucher extends Card {

    public CreditVoucher() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SacrificeSelfCost(),
                        ChooseCardEffect.upTo(Subjects.cardsInYourHand(null), Integer.MAX_VALUE,
                                "cards from your hand to shuffle into your library"),
                        PutCardsIntoLibraryEffect.shuffledIn(Subjects.cardsThisWay(StepKind.CHOOSE))
                                .alwaysShuffling(Subjects.you()),
                        new DrawCardEffect(new AmountThisWay(StepKind.SHUFFLE_INTO_LIBRARY), Subjects.you())),
                "{2}, {T}, Sacrifice this artifact: Shuffle any number of cards from your hand into your library, then draw that many cards."));
    }
}
