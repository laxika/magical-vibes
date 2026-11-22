package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "174")
@CardRegistration(set = "PIO", collectorNumber = "158")
public class ValakutAwakening extends Card {

    public ValakutAwakening() {
        setBackFaceCard(new ValakutStoneforge());
        setModalDoubleFaced(true);

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put any number of cards from your hand on the bottom of your library, then draw that many cards plus one",
                        List.of(
                                ChooseCardEffect.upTo(Subjects.cardsInYourHand(null), Integer.MAX_VALUE,
                                        "cards from your hand to put on the bottom of your library"),
                                PutCardsIntoLibraryEffect.onBottom(Subjects.cardsThisWay(StepKind.CHOOSE)),
                                new DrawCardEffect(new Sum(new AmountThisWay(StepKind.PUT_INTO_LIBRARY),
                                        new Fixed(1))))),
                new ChooseOneEffect.ChooseOneOption("Valakut Stoneforge", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ValakutStoneforge";
    }
}
