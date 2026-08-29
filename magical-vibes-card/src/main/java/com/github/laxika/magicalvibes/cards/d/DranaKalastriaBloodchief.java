package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "107")
public class DranaKalastriaBloodchief extends Card {

    public DranaKalastriaBloodchief() {
        // {X}{B}{B}: Target creature gets -0/-X until end of turn and Drana gets +X/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B}{B}",
                List.of(
                        new BoostTargetCreatureEffect(new Fixed(0), new Scaled(new XValue(), -1)),
                        new BoostSelfEffect(new XValue(), new Fixed(0))
                ),
                "{X}{B}{B}: Target creature gets -0/-X until end of turn and Drana gets +X/+0 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
