package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "43")
@CardRegistration(set = "EMA", collectorNumber = "16")
@CardRegistration(set = "VMA", collectorNumber = "31")
@CardRegistration(set = "C15", collectorNumber = "71")
@CardRegistration(set = "C17", collectorNumber = "61")
@CardRegistration(set = "CM2", collectorNumber = "30")
public class JarethLeonineTitan extends Card {

    public JarethLeonineTitan() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BoostSelfEffect(7, 7));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.SELF)),
                "{W}: Jareth gains protection from the color of your choice until end of turn."
        ));
    }
}
