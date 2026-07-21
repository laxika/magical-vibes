package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "HOU", collectorNumber = "17")
public class OketrasAvenger extends Card {

    public OketrasAvenger() {
        // Exert: "You may exert this creature as it attacks. When you do, prevent all combat damage
        // that would be dealt to it this turn." Modeled as an optional attack trigger (matching Gust
        // Walker). Choosing to exert also keeps the creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        PreventDamageEffect.allCombatToSelf(),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Oketra's Avenger as it attacks? (Prevent all combat damage that would be dealt to it this turn.)"
        ));
    }
}
