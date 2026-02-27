package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetUntapWhileSourceTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "198")
public class RustTick extends Card {

    public RustTick() {
        // Static: "You may choose not to untap this creature during your untap step."
        addEffect(EffectSlot.STATIC, new MayNotUntapDuringUntapStepEffect());

        // {1}, {T}: Tap target artifact. It doesn't untap during its controller's untap step
        // for as long as Rust Tick remains tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new TapTargetPermanentEffect(),
                        new PreventTargetUntapWhileSourceTappedEffect()
                ),
                "{1}, {T}: Tap target artifact. It doesn't untap during its controller's untap step for as long as Rust Tick remains tapped.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));
    }
}
