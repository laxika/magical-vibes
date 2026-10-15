package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureWithManaValueEffect;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "14")
public class CosmicSovereign extends Card {

    public CosmicSovereign() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: Cosmic Sovereign gets +1/+0 until end of turn."));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConjureRandomCreatureWithManaValueEffect(new SourcePower()));
    }
}
