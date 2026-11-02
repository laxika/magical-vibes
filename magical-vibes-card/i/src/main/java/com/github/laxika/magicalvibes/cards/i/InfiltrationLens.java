package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "167")
public class InfiltrationLens extends Card {

    public InfiltrationLens() {
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new MayEffect(new DrawCardEffect(2), "Draw two cards?"));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
