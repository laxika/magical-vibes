package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ALL", collectorNumber = "95")
@CardRegistration(set = "TSB", collectorNumber = "79")
public class HailStorm extends Card {

    public HailStorm() {
        addSimultaneousSpellEffects(DealDamageEffect.toEachCreature(2, new PermanentIsAttackingPredicate()),
                DealDamageEffect.toEachCreature(1, new PermanentControlledBySourceControllerPredicate()),
                DealDamageEffect.to(Subjects.you(), 1));
    }
}
