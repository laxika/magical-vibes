package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetSpellUncounterableEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "222")
public class VexingShusher extends Card {

    public VexingShusher() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addActivatedAbility(new ActivatedAbility(false, "{R/G}", List.of(new MakeTargetSpellUncounterableEffect()),
                "{R/G}: Target spell can't be countered."));
    }
}
