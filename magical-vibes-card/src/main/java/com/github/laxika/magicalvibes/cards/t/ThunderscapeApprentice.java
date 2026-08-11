package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "174")
public class ThunderscapeApprentice extends Card {

    public ThunderscapeApprentice() {
        addActivatedAbility(new ActivatedAbility(true, "{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                "{B}, {T}: Target player loses 1 life."));

        addActivatedAbility(new ActivatedAbility(true, "{G}",
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{G}, {T}: Target creature gets +1/+1 until end of turn.",
                TargetFilters.creature()));
    }
}
