package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TDC", collectorNumber = "44")
@CardRegistration(set = "TDC", collectorNumber = "84")
public class BroodcallerScourge extends Card {

    public BroodcallerScourge() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DRAGON),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                        .boundedByEventValue(),
                                "Put a permanent card with mana value less than or equal to that damage from your hand onto the battlefield?"),
                        false,
                        true));
    }
}
