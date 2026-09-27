package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyReduceCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "21")
public class BloodsproutTalisman extends Card {

    public BloodsproutTalisman() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}, Pay 1 life: Choose a nonland card in your hand. It perpetually gains
        // "This spell costs {1} less to cast."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new ChooseCardFromHandToPerpetuallyReduceCastCostEffect(1)),
                "{T}, Pay 1 life: Choose a nonland card in your hand. It perpetually gains \"This spell costs {1} less to cast.\""
        ));
    }
}
