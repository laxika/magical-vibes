package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "106")
public class ExhibitionMagician extends Card {

    public ExhibitionMagician() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 green and white Citizen creature token",
                        new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN))),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a Treasure token",
                        CreateTokenEffect.ofTreasureToken(1))
        )));
    }
}
