package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WTH", collectorNumber = "13")
public class EmpyrialArmor extends Card {

    public EmpyrialArmor() {
        // Enchant creature. Enchanted creature gets +1/+1 for each card in your hand.
        // "your" is the Aura's controller, so CONTROLLER (unlike Righteous Authority's ATTACHED_CONTROLLER).
        CardsInHand hand = new CardsInHand(CountScope.CONTROLLER);
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        hand, hand, GrantScope.ENCHANTED_CREATURE));
    }
}
