package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "47")
public class AtomicMicrosizer extends Card {

    public AtomicMicrosizer() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.attacks(), new MakeCreatureUnblockableEffect())
                .addTrigger(Triggers.attacks(), new SetBasePowerToughnessEffect(1, 1));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
