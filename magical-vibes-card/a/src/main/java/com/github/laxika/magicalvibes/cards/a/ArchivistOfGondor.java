package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForMonarchEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "18")
@CardRegistration(set = "LTC", collectorNumber = "101")
@CardRegistration(set = "LTC", collectorNumber = "420")
public class ArchivistOfGondor extends Card {

    public ArchivistOfGondor() {
        var commanderDamage = new TriggeringPermanentConditionalEffect(
                new PermanentIsCommanderPredicate(true),
                new ConditionalEffect(new NoMonarch(), new BecomeMonarchEffect()));
        addTrigger(Triggers.creatureDealsCombatDamageToOpponent(), commanderDamage);
        addTrigger(Triggers.creatureDealsCombatDamageToItsOwner(), commanderDamage);

        addTrigger(Triggers.endStep(ActivePlayer.ANY), new DrawCardForMonarchEndStepEffect());
    }
}
