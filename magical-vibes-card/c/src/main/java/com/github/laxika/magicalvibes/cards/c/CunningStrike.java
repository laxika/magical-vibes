package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "150")
public class CunningStrike extends Card {

    public CunningStrike() {
        setAllowSharedTargets(true);

        DealDamageToTargetCreatureEffect creatureDamage = new DealDamageToTargetCreatureEffect(2);
        DealDamageToTargetPlayerOrPlaneswalkerEffect playerDamage =
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2);
        target(TargetFilters.creature()).bindEffect(creatureDamage);
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"
        )).bindEffect(playerDamage);
        addSimultaneousSpellEffects(creatureDamage, playerDamage);
        addSpellEffect(new DrawCardEffect(1));
    }
}
