package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "26")
@CardRegistration(set = "BLC", collectorNumber = "60")
public class PyreswipeHawk extends Card {

    public PyreswipeHawk() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                new Fixed(0)));

        target(TargetFilters.artifact(), 0, 1).addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.wheneverYouExpend(6,
                        List.of(new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD))));
    }
}
