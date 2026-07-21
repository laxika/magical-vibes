package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "28")
public class VizierOfTheTrue extends Card {

    public VizierOfTheTrue() {
        // Exert: "You may exert this creature as it attacks." Modeled as an optional attack trigger
        // (matching Glorybringer). The target is chosen when the trigger is put on the stack; the
        // "you may exert" is confirmed at resolution. Choosing to exert also keeps the creature tapped
        // through its next untap step.
        //
        // "Whenever you exert a creature, tap target creature an opponent controls." The engine has no
        // exert-event slot, so the only exert it can observe is this creature's own exert as it attacks —
        // the tap is bundled onto the exert when it is accepted (matching Trueheart Twins).
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature an opponent controls"
        )).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Vizier of the True as it attacks? (Tap target creature an opponent controls.)"
        ));
    }
}
