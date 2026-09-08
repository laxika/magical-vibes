package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "110")
public class MonoistCircuitFeeder extends Card {

    public MonoistCircuitFeeder() {
        PermanentCount artifacts = new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(artifacts, new Fixed(0)));
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new BoostTargetCreatureEffect(new Fixed(0), new Scaled(artifacts, -1)));
    }
}
