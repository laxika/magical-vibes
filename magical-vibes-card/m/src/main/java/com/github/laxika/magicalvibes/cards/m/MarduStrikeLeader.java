package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "75")
public class MarduStrikeLeader extends Card {

    public MarduStrikeLeader() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{B}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                1, "Warrior", 2, 1, CardColor.BLACK, List.of(CardSubtype.WARRIOR),
                Set.of(), Set.of()
        ));
    }
}
