package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceByEnteringPowerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "24")
public class QuickbeastAmulet extends Card {

    public QuickbeastAmulet() {
        addStaticEffect(new AttachedBoostEffect(
                new SourceIntensity(), new SourceIntensity(), GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new IntensifySourceByEnteringPowerEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
