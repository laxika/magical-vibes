package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "YNEO", collectorNumber = "16")
public class SoulServitude extends Card {

    public SoulServitude() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."))
                .addEffect(EffectSlot.SPELL,
                        new TargetPlayerSacrificesNontokenCreatureThenConjuresDuplicateEffect(
                                PlayerRelation.ANY, -1, true));
    }
}
