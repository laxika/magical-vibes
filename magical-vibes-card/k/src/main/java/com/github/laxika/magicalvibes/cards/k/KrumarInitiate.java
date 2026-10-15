package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "84")
public class KrumarInitiate extends Card {

    public KrumarInitiate() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{B}",
                List.of(new PayXLifeCost(), new EndureEffect(new XValue())),
                "{X}{B}, {T}, Pay X life: This creature endures X. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
