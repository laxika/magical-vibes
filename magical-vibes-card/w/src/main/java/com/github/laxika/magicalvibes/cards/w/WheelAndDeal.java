package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ONS", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "1262")
public class WheelAndDeal extends Card {

    public WheelAndDeal() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ), 0, 99)
                .addSpellEffect(new DiscardHandEffect(Subjects.targetPlayer()))
                .addSpellEffect(new DrawCardEffect(7, Subjects.targetPlayer()));
        addSpellEffect(new DrawCardEffect(1));
    }
}
