package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "140")
public class IncendiaryOracle extends Card {

    public IncendiaryOracle() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(new BoostSelfEffect(1, 0)),
                "{1}{R}: This creature gets +1/+0 until end of turn."));
        addEffect(EffectSlot.STATIC, new ExileCreaturesDamagedBySourceInsteadOfDyingEffect());
    }
}
