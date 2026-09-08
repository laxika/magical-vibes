package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "241")
public class ViciousRivalry extends Card {

    public ViciousRivalry() {
        addEffect(EffectSlot.SPELL, new PayXLifeCost());
        PermanentAnyOfPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentMaxManaValueXPredicate(), artifactOrCreature))));
    }
}
