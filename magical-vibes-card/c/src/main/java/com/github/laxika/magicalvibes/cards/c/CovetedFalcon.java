package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetGroupMarkerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "48")
@CardRegistration(set = "MKM", collectorNumber = "393")
public class CovetedFalcon extends Card {

    public CovetedFalcon() {
        addMorph("{1}{U}");

        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a permanent you own but don't control"
        )).addEffect(EffectSlot.ON_ATTACK,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT));

        TargetGroupMarkerEffect opponent = new TargetGroupMarkerEffect();
        TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect gainControl =
                new TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanentEffect(2);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(opponent);
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent you control"
        ), 0, Integer.MAX_VALUE).bindEffect(gainControl);
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), opponent, gainControl);
    }
}
