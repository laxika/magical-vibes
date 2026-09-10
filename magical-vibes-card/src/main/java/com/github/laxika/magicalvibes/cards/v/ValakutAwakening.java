package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "174")
public class ValakutAwakening extends Card {

    public ValakutAwakening() {
        setBackFaceCard(new ValakutStoneforge());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put any number of cards from your hand on the bottom of your library, then draw that many cards plus one",
                        new PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect()),
                new ChooseOneEffect.ChooseOneOption("Valakut Stoneforge", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "ValakutStoneforge";
    }
}
