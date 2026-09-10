package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "76")
public class SeaGateRestoration extends Card {

    public SeaGateRestoration() {
        setBackFaceCard(new SeaGateReborn());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Sea Gate Restoration", List.of(
                        new DrawCardEffect(new Sum(
                                new CardsInHand(CountScope.CONTROLLER), new Fixed(1))),
                        new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME)
                )),
                new ChooseOneEffect.ChooseOneOption("Sea Gate, Reborn", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SeaGateReborn";
    }
}
