package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CommittedCrimeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "OTJ", collectorNumber = "68")
public class SlickshotVaultBuster extends Card {

    public SlickshotVaultBuster() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CommittedCrimeThisTurn(), new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
