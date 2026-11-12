package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "17")
@CardRegistration(set = "AKR", collectorNumber = "29")
public class OketrasAvenger extends Card {

    public OketrasAvenger() {
        // Exert: "You may exert this creature as it attacks. When you do, prevent all combat damage
        // that would be dealt to it this turn." Modeled as an optional attack trigger (matching Gust
        // Walker). Choosing to exert also keeps the creature tapped through its next untap step.
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        SkipNextUntapEffect.exert(),
                        new QueueReflexiveAbilityEffect(PreventDamageEffect.allCombatToSelf())
                ),
                "Exert Oketra's Avenger as it attacks? (Prevent all combat damage that would be dealt to it this turn.)"
        ));
    }
}
