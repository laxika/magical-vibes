package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "DRK", collectorNumber = "9")
public class FireAndBrimstone extends Card {

    public FireAndBrimstone() {
        DealDamageToPlayersEffect targetDamage =
                new DealDamageToPlayersEffect(4, DamageRecipient.TARGET_PLAYER);
        target(new PlayerPredicateTargetFilter(
                new PlayerAttackedThisTurnPredicate(),
                "Target must be a player who attacked this turn"
        )).bindEffect(targetDamage);
        addSimultaneousSpellEffects(targetDamage,
                new DealDamageToPlayersEffect(4, DamageRecipient.CONTROLLER));
    }
}
