package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "WHO", collectorNumber = "172")
@CardRegistration(set = "WHO", collectorNumber = "777")
public class ClockworkDroid extends Card {

    public ClockworkDroid() {
        // Exert: "You may exert this creature as it attacks. When you do, it can't be blocked this
        // turn and you scry 1." Choosing to exert also keeps the creature tapped through its next
        // untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new MakeCreatureUnblockableEffect(true),
                        new ScryEffect(1),
                        new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true)
                ),
                "Exert Clockwork Droid as it attacks? (It can't be blocked this turn and you scry 1.)"
        ));
    }
}
