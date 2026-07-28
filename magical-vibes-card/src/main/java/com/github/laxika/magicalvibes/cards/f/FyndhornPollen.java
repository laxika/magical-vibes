package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "245")
public class FyndhornPollen extends Card {

    public FyndhornPollen() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // All creatures get -1/-0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.ALL_CREATURES));

        // {1}{G}: All creatures get -1/-0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new BoostAllCreaturesEffect(-1, 0)),
                "{1}{G}: All creatures get -1/-0 until end of turn."));
    }
}
