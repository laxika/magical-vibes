package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "99")
public class IntimidationBolt extends Card {

    public IntimidationBolt() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureEffect(3))
                .addSpellEffect(new CantAttackThisTurnEffect(
                        Subjects.except(Subjects.allCreatures(), Subjects.targetDeclaredElsewhere())));
    }
}
