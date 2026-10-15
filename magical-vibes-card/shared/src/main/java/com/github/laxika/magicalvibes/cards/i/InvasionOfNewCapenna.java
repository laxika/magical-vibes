package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HolyFrazzleCannon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "238")
public class InvasionOfNewCapenna extends Card {

    public InvasionOfNewCapenna() {
        setBackFaceCard(new HolyFrazzleCannon());

        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        var artifactOrCreatureOpponentControls = new PermanentAllOfPredicate(List.of(
                artifactOrCreature,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        artifactOrCreature,
                        new ExileTargetPermanentEffect(artifactOrCreatureOpponentControls),
                        "an artifact or creature"),
                "Sacrifice an artifact or creature?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "HolyFrazzleCannon";
    }
}
