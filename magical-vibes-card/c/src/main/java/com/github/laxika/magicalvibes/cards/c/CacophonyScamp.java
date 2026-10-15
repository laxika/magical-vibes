package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

@CardRegistration(set = "ONE", collectorNumber = "124")
public class CacophonyScamp extends Card {

    public CacophonyScamp() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new SacrificeSelfThenEffect(new ProliferateEffect()),
                        "You may sacrifice it. If you do, proliferate."));
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
