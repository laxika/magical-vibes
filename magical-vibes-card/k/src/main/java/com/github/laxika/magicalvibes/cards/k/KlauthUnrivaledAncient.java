package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "SLD", collectorNumber = "2499")
@CardRegistration(set = "AFC", collectorNumber = "50")
public class KlauthUnrivaledAncient extends Card {

    public KlauthUnrivaledAncient() {
        addEffect(EffectSlot.ON_ATTACK, new AwardPersistentAnyColorManaEffect(
                new TotalPowerOfControlledCreatures(new PermanentIsAttackingPredicate()),
                ManaSpendRestriction.SPELL_ONLY, true));
    }
}
