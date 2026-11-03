package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "39")
@CardRegistration(set = "DSC", collectorNumber = "66")
public class GigglingSkitterspike extends Card {

    public GigglingSkitterspike() {
        addTrigger(Triggers.attacks(),
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.becomesTargetOfSpell(),
                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new MonstrosityEffect(5)),
                "{5}: Monstrosity 5."
        ));
    }
}
