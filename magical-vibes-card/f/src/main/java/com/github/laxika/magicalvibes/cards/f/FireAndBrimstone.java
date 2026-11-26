package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "DRK", collectorNumber = "9")
public class FireAndBrimstone extends Card {

    public FireAndBrimstone() {
        DealDamageEffect targetDamage =
                DealDamageEffect.to(Subjects.targetPlayer(), 4);
        target(new PlayerPredicateTargetFilter(
                new PlayerAttackedThisTurnPredicate(),
                "Target must be a player who attacked this turn"
        )).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage,
                DealDamageEffect.to(Subjects.you(), 4));
    }
}
