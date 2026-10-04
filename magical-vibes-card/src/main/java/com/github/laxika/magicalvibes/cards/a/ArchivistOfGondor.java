package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForMonarchEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

@CardRegistration(set = "LTC", collectorNumber = "18")
@CardRegistration(set = "LTC", collectorNumber = "101")
@CardRegistration(set = "LTC", collectorNumber = "420")
public class ArchivistOfGondor extends Card {

    public ArchivistOfGondor() {
        var commanderDamage = new TriggeringPermanentConditionalEffect(
                new PermanentIsCommanderPredicate(true),
                new ConditionalEffect(new NoMonarch(), new BecomeMonarchEffect()));
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT, commanderDamage);
        addEffect(EffectSlot.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER, commanderDamage);

        addEffect(EffectSlot.END_STEP_TRIGGERED, new DrawCardForMonarchEndStepEffect());
    }
}
