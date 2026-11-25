package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AER", collectorNumber = "84")
@CardRegistration(set = "KLR", collectorNumber = "129")
public class HungryFlames extends Card {

    public HungryFlames() {
        DealDamageEffect creatureDamage = DealDamageEffect.toTargetCreature(3);
        DealDamageEffect playerDamage =
                DealDamageEffect.toTargetPlayerOrPlaneswalker(2);
        target(TargetFilters.creature()).bindEffect(creatureDamage);
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker."
        )).bindEffect(playerDamage);
        addSimultaneousSpellEffects(creatureDamage, playerDamage);
    }
}
