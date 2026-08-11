package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "65")
public class MasterfulReplication extends Card {

    private static final PermanentPredicate ARTIFACTS_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsArtifactPredicate(),
            new PermanentControlledBySourceControllerPredicate()));

    public MasterfulReplication() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create two 3/3 colorless Golem artifact creature tokens",
                        new CreateTokenEffect(2, "Golem", 3, 3, null,
                                List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target artifact you control. Each other artifact you control becomes a copy of that artifact until end of turn",
                        new EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
                                ARTIFACTS_YOU_CONTROL, ARTIFACTS_YOU_CONTROL),
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentIsArtifactPredicate(),
                                "Target must be an artifact you control.")))));
    }
}
