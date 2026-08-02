package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastInstantsOrActivateNonManaAbilitiesDuringCombatEffect;

/**
 * Hand to Hand — "During combat, players can't cast instant spells or activate abilities that
 * aren't mana abilities."
 */
@CardRegistration(set = "TMP", collectorNumber = "180")
public class HandToHand extends Card {

    public HandToHand() {
        addEffect(EffectSlot.STATIC, new PlayersCantCastInstantsOrActivateNonManaAbilitiesDuringCombatEffect());
    }
}
