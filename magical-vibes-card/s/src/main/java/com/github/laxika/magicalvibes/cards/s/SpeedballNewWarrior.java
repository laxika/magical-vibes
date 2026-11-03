package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "227")
public class SpeedballNewWarrior extends Card {

    public SpeedballNewWarrior() {
        addTrigger(Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new BoostSelfEffect(2, 2),
                new MayEffect(new ChooseNewTargetsForTargetSpellEffect(), "Choose new targets for the spell?"));
    }
}
