package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "3")
public class AuriokBladewarden extends Card {

    public AuriokBladewarden() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(new SourcePower(), new SourcePower())),
                "{T}: Target creature gets +X/+X until end of turn, where X is this creature's power."
        ));
    }
}
