package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;

@CardRegistration(set = "TLA", collectorNumber = "139")
public class FirebendingStudent extends Card {

    public FirebendingStudent() {
        addEffect(EffectSlot.ON_ATTACK,
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, new SourcePower()));
    }
}
