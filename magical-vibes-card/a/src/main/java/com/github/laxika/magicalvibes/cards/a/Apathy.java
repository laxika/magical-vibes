package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardThenUntapEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WTH", collectorNumber = "33")
public class Apathy extends Card {

    public Apathy() {
        // "Enchant creature. Enchanted creature doesn't untap during its controller's untap step."
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());

        // "At the beginning of the upkeep of enchanted creature's controller, that player may
        // discard a card at random. If the player does, untap that creature."
        addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new MayPayManaEffect(null, new DiscardRandomCardThenUntapEnchantedCreatureEffect(),
                        "discard a card at random to untap the creature", MayPayPayer.ENCHANTED_CONTROLLER));
    }
}
