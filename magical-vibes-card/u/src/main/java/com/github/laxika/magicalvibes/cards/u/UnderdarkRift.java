package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.RollD10Effect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "62")
public class UnderdarkRift extends Card {

    public UnderdarkRift() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        var artifactCreatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ExileSelfCost(),
                        new RollD10Effect(new PutTargetPermanentIntoLibraryNFromTopEffect(
                                new EventValue()))),
                "{5}, {T}, Exile this land: Roll a d10. Put target artifact, creature, or planeswalker "
                        + "into its owner's library just beneath the top X cards of that library, where X "
                        + "is the result. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(
                        artifactCreatureOrPlaneswalker,
                        "Target must be an artifact, creature, or planeswalker"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
