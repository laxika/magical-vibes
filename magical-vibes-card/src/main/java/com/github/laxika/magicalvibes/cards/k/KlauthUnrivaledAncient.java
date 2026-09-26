package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;

@CardRegistration(set = "SLD", collectorNumber = "2499")
public class KlauthUnrivaledAncient extends Card {

    public KlauthUnrivaledAncient() {
        addEffect(EffectSlot.ON_ATTACK, new AwardPersistentAnyColorManaEffect(
                new EventValue(), ManaSpendRestriction.SPELL_ONLY, true));
    }
}
