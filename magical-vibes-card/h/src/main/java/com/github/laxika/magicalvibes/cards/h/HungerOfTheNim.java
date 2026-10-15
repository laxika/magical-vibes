package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "46")
public class HungerOfTheNim extends Card {

    public HungerOfTheNim() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER),
                new Fixed(0)
        ));
    }
}
