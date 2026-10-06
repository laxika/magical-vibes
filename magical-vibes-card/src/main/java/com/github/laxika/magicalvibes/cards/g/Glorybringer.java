package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "134")
@CardRegistration(set = "AKR", collectorNumber = "157")
@CardRegistration(set = "TDC", collectorNumber = "215")
public class Glorybringer extends Card {

    public Glorybringer() {
        // Flying and haste are Scryfall-loaded keywords — no wiring needed.

        // Exert: "You may exert this creature as it attacks. When you do, it deals 4 damage to
        // target non-Dragon creature an opponent controls." Exerting is chosen as it attacks; the
        // "when you do" reflexive trigger then picks its target (matching Ahn-Crop Crasher).
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                        new QueueReflexiveAbilityEffect(new DealDamageToTargetCreatureEffect(4,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))
                                ))))
                ),
                "Exert Glorybringer as it attacks? (It deals 4 damage to target non-Dragon creature an opponent controls.)"
        ));
    }
}
