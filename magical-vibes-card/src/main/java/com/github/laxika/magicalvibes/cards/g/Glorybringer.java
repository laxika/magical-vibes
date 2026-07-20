package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "134")
public class Glorybringer extends Card {

    public Glorybringer() {
        // Flying and haste are Scryfall-loaded keywords — no wiring needed.

        // Exert: "You may exert this creature as it attacks. When you do, it deals 4 damage to
        // target non-Dragon creature an opponent controls." Modeled as an optional attack trigger
        // (matching Ahn-Crop Crasher). The target is chosen when the trigger is put on the stack;
        // the "you may exert" is confirmed at resolution. Choosing to exert also keeps the creature
        // tapped through its next untap step.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))
                )),
                "Target must be a non-Dragon creature an opponent controls"
        )).addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(4),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Glorybringer as it attacks? (It deals 4 damage to target non-Dragon creature an opponent controls.)"
        ));
    }
}
