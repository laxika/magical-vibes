package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "119")
@CardRegistration(set = "SLD", collectorNumber = "2211")
@CardRegistration(set = "CMD", collectorNumber = "132")
@CardRegistration(set = "CMA", collectorNumber = "86")
public class Pyrohemia extends Card {

    public Pyrohemia() {
        // At the beginning of the end step, if no creatures are on the battlefield, sacrifice this enchantment.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyPlayerControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate())),
                new SacrificeSelfEffect());

        // {R}: This enchantment deals 1 damage to each creature and each player.
        addActivatedAbility(new ActivatedAbility(false, "{R}",
                List.of(new MassDamageEffect(1, true)),
                "{R}: This enchantment deals 1 damage to each creature and each player."));
    }
}
