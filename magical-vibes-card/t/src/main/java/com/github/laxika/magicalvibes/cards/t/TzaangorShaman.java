package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "146")
public class TzaangorShaman extends Card {

    public TzaangorShaman() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                        CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect())));
    }
}
