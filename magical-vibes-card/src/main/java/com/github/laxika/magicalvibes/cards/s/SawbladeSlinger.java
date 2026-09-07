package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "217")
public class SawbladeSlinger extends Card {

    public SawbladeSlinger() {
        PermanentPredicate opponentControls = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        PermanentPredicate artifactAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                opponentControls));
        PermanentPredicate zombieAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                opponentControls));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact an opponent controls",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                artifactAnOpponentControls,
                                "Target must be an artifact an opponent controls")),
                new ChooseOneEffect.ChooseOneOption(
                        "This creature fights target Zombie an opponent controls",
                        new SourceFightsTargetCreatureEffect(),
                        new PermanentPredicateTargetFilter(
                                zombieAnOpponentControls,
                                "Target must be a Zombie an opponent controls"))
        ), true));
    }
}
