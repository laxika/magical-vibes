package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "31")
public class SeaSnidd extends Card {

    public SeaSnidd() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN, null, true)),
                "{T}: Target land becomes the basic land type of your choice until end of turn.",
                TargetFilters.land()
        ));
    }
}
