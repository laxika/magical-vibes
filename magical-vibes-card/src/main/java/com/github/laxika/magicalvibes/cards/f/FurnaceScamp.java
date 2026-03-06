package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDealDamageToDamagedPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "84")
public class FurnaceScamp extends Card {

    public FurnaceScamp() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new SacrificeSelfAndDealDamageToDamagedPlayerEffect(3),
                        "You may sacrifice it. If you do, it deals 3 damage to that player."));
    }
}
