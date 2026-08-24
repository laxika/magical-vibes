package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "28")
public class LivingAirship extends Card {

    public LivingAirship() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{G}", List.of(new RegenerateEffect()),
                "{2}{G}: Regenerate Living Airship."));
    }
}
