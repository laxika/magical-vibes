package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "143")
public class MishrasOnslaught extends Card {

    public MishrasOnslaught() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 1/1 colorless Soldier artifact creature tokens",
                        new CreateTokenEffect(2, "Soldier", 1, 1, (CardColor) null,
                                List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +2/+0 until end of turn",
                        new BoostAllOwnCreaturesEffect(2, 0))
        )));
    }
}
