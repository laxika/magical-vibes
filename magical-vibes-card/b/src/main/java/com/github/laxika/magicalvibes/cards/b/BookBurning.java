package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "JUD", collectorNumber = "80")
public class BookBurning extends Card {

    public BookBurning() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"))
                .addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), DamageFromSourceCost.of(6),
                                new MillEffect(6, Subjects.targetPlayer()))
                        .withPrompt("Have it deal 6 damage to you? If no one does, target player mills six cards.")
                        .payingTogether());
    }
}
