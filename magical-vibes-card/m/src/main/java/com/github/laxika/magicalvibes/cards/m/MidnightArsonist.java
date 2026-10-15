package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasManaAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "27")
@CardRegistration(set = "VOC", collectorNumber = "65")
public class MidnightArsonist extends Card {

    public MidnightArsonist() {
        PermanentPredicate artifactWithoutManaAbility = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentHasManaAbilityPredicate())));

        targetUpTo(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER),
                new PermanentPredicateTargetFilter(
                        artifactWithoutManaAbility, "Target must be an artifact without a mana ability"),
                100
        ).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DestroyEachTargetPermanentEffect(artifactWithoutManaAbility));
    }
}
