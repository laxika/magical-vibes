package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "135")
public class ThranWeaponry extends Card {

    public ThranWeaponry() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{4}"));
        addStaticEffect(new MayNotUntapDuringUntapStepEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostAllCreaturesWhileSourceTappedEffect(2, 2)),
                "{2}, {T}: All creatures get +2/+2 for as long as this artifact remains tapped."
        ));
    }
}
