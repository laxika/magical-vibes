package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AKH", collectorNumber = "117")
public class AhnCropCrasher extends Card {

    public AhnCropCrasher() {
        // Exert: "You may exert this creature as it attacks. When you do, target creature can't block
        // this turn." Modeled as an optional attack trigger (matching Glory-Bound Initiate). The
        // target is chosen when the trigger is put on the stack; the "you may exert" is confirmed at
        // resolution. Choosing to exert also keeps the creature tapped through its next untap step.
        // (Haste is a Scryfall-loaded keyword — no wiring needed.)
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target creature"
        )).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Ahn-Crop Crasher as it attacks? (Target creature can't block this turn.)"
        ));
    }
}
