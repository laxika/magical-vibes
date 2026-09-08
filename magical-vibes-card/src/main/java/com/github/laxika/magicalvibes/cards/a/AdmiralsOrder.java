package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "31")
public class AdmiralsOrder extends Card {

    public AdmiralsOrder() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{U}")),
                new Raid(),
                false));

        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
