package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "67")
@CardRegistration(set = "LTC", collectorNumber = "148")
public class RidersOfRohan extends Card {

    public RidersOfRohan() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{R}{W}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Human Knight", 2, 2, CardColor.RED,
                        List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT),
                        Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
    }
}
