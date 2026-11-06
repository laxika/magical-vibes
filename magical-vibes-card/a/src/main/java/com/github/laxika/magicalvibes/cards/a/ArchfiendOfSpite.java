package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C19", collectorNumber = "14")
public class ArchfiendOfSpite extends Card {

    public ArchfiendOfSpite() {
        addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE,
                new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect());
        addCastingOption(new MadnessCast("{3}{B}{B}"));
    }
}
