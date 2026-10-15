package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "155")
@CardRegistration(set = "DD1", collectorNumber = "54")
@CardRegistration(set = "EVG", collectorNumber = "54")
public class BoggartShenanigans extends Card {

    public BoggartShenanigans() {
        // Whenever another Goblin you control is put into a graveyard from the battlefield,
        // you may have this enchantment deal 1 damage to target player or planeswalker.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, new TriggeringPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                new MayEffect(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1),
                        "Deal 1 damage to target player or planeswalker?")));
    }
}
