package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "176")
public class NoxiousAssault extends Card {

    public NoxiousAssault() {
        addSpellEffect(BoostEffect.creaturesYouControl(2, 2));
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureBlocks(),
                new GivePoisonCountersEffect(1, Subjects.controllerOf(Subjects.target())))));
    }
}
