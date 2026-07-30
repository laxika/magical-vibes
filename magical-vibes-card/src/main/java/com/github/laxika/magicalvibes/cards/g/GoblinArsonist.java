package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M12", collectorNumber = "136")
public class GoblinArsonist extends Card {

    public GoblinArsonist() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new DealDamageToAnyTargetEffect(1),
                "Have Goblin Arsonist deal 1 damage to any target?"));
    }
}
