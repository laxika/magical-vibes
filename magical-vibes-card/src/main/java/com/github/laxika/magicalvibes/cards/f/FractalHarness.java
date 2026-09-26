package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersAndAttachToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnEnchantedCreatureEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "268")
public class FractalHarness extends Card {

    public FractalHarness() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateXTokenWithXCountersAndAttachToSourceEffect(
                        "Fractal", 0, 0, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL),
                        CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_ATTACK, new DoublePlusOneCountersOnEnchantedCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
