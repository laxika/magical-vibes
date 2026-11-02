package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "139")
public class KorvoldAndTheNobleThief extends Card {

    public KorvoldAndTheNobleThief() {
        addTrigger(Triggers.chapter(1), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.chapter(2), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.chapter(3),
                new ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect(3));
        setSagaChapterTargetFilter(3, Set.of(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent")));
    }
}
