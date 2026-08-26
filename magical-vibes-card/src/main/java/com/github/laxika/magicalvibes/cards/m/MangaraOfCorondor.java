package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "28")
public class MangaraOfCorondor extends Card {

    public MangaraOfCorondor() {
        // {T}: Exile Mangara and target permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileTargetPermanentThenEffect(
                        new ExileSelfEffect(), ThenEffectRecipient.CONTROLLER)),
                "{T}: Exile Mangara and target permanent.",
                TargetFilters.permanent()
        ));
    }
}
