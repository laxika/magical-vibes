package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "155")
public class BoggartShenanigans extends Card {

    public BoggartShenanigans() {
        // Whenever another Goblin you control is put into a graveyard from the battlefield,
        // you may have this enchantment deal 1 damage to target player or planeswalker.
        // Boggart Shenanigans is a non-creature enchantment, so it can never be the dying
        // creature ("another" is satisfied automatically).
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.GOBLIN),
                new MayEffect(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                        "Deal 1 damage to target player or planeswalker?")));
    }
}
