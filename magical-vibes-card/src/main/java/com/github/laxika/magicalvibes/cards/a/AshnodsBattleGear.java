package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "296")
public class AshnodsBattleGear extends Card {

    public AshnodsBattleGear() {
        // Static: "You may choose not to untap this artifact during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // {2}, {T}: Target creature you control gets +2/-2 for as long as this artifact remains tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BoostTargetCreatureWhileSourceTappedEffect(2, -2)),
                "{2}, {T}: Target creature you control gets +2/-2 for as long as this artifact remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate()
                        )),
                        "Target must be a creature you control"
                )
        ));
    }
}
