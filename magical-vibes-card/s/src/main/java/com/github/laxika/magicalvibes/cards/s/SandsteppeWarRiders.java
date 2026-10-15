package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "39")
@CardRegistration(set = "MOC", collectorNumber = "126")
public class SandsteppeWarRiders extends Card {

    public SandsteppeWarRiders() {
        PermanentAllOfPredicate artifactToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsTokenPredicate()));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new BolsterEffect(
                new DistinctPermanentNamesAmongControlled(artifactToken, CountScope.CONTROLLER)));
    }
}
