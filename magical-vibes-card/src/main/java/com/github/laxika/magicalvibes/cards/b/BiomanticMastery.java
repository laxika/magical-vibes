package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "DIS", collectorNumber = "142")
public class BiomanticMastery extends Card {

    public BiomanticMastery() {
        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
        PermanentCount creatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER);

        target(playerTarget).addEffect(EffectSlot.SPELL, new DrawCardEffect(creatures));
        target(playerTarget).addEffect(EffectSlot.SPELL, new DrawCardEffect(creatures));
    }
}
