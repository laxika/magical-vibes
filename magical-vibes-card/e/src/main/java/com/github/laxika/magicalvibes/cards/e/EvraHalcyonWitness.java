package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithCreatureStatEffect;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "16")
public class EvraHalcyonWitness extends Card {

    public EvraHalcyonWitness() {
        // {4}: Exchange your life total with Evra, Halcyon Witness's power.
        addActivatedAbility(new ActivatedAbility(false, "{4}",
                List.of(new ExchangeLifeTotalWithCreatureStatEffect(ExchangeLifeTotalWithCreatureStatEffect.Stat.POWER)),
                "{4}: Exchange your life total with Evra, Halcyon Witness's power."));
    }
}
