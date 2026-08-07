package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "266")
public class Dracoplasm extends Card {

    public Dracoplasm() {
        // As-enters replacement (CR 614.1c): the sacrificed creatures' totals become its base P/T.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect());
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}
