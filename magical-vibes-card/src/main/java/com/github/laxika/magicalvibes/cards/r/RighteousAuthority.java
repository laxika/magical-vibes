package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "189")
public class RighteousAuthority extends Card {

    public RighteousAuthority() {
        // Enchant creature. Enchanted creature gets +1/+1 for each card in its controller's hand.
        // ATTACHED_CONTROLLER (not CONTROLLER) — "its" is the enchanted creature, not the Aura.
        CardsInHand hand = new CardsInHand(CountScope.ATTACHED_CONTROLLER);
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        hand, hand, GrantScope.ENCHANTED_CREATURE))
                // At the beginning of the draw step of enchanted creature's controller, that player
                // draws an additional card. Slot bakes that player as targetId.
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_DRAW_TRIGGERED,
                        new DrawCardForTargetPlayerEffect(1));
    }
}
