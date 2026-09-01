package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "62")
public class LostInSpace extends Card {

    public LostInSpace() {
        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));

        target(new PermanentPredicateTargetFilter(artifactOrCreature,
                "Target must be an artifact or creature."))
                .addEffect(EffectSlot.SPELL,
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0, artifactOrCreature))
                .addEffect(EffectSlot.SPELL, new SurveilEffect(1));
    }
}
