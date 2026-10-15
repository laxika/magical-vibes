package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "300")
public class ElixirOfVitality extends Card {

    public ElixirOfVitality() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new GainLifeEffect(4)),
                "{T}, Sacrifice Elixir of Vitality: You gain 4 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{8}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(8)),
                "{8}, {T}, Sacrifice Elixir of Vitality: You gain 8 life."
        ));
    }
}
