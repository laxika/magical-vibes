package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "563")
public class UltimaWeapon extends Card {

    public UltimaWeapon() {
        addStaticEffect(new StaticBoostEffect(7, 7, GrantScope.EQUIPPED_CREATURE));
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.attacks(), new DestroyTargetPermanentEffect());
        addActivatedAbility(new EquipActivatedAbility("{7}"));
    }
}
