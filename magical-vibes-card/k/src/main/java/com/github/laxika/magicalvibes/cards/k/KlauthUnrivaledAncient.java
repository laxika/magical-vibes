package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2499")
@CardRegistration(set = "AFC", collectorNumber = "50")
public class KlauthUnrivaledAncient extends Card {

    public KlauthUnrivaledAncient() {
        addTrigger(Triggers.attacks(), new AwardPersistentAnyColorManaEffect(
                new TotalPowerOfControlledCreatures(new PermanentIsAttackingPredicate()),
                ManaSpendRestriction.SPELL_ONLY, true));
    }
}
