package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "170")
public class SlimyKavu extends Card {

    public SlimyKavu() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantBasicLandTypeToTargetEffect(
                        Duration.UNTIL_END_OF_TURN, CardSubtype.SWAMP, true)),
                "{T}: Target land becomes a Swamp until end of turn.",
                TargetFilters.land()
        ));
    }
}
