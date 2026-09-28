package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCardsFromHandThenMayCastOneEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "AFC", collectorNumber = "46")
public class ExtractBrain extends Card {

    public ExtractBrain() {
        // Target opponent chooses X cards from their hand. Look at those cards. You may cast a
        // spell from among them without paying its mana cost.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.SPELL,
                        new TargetPlayerChoosesCardsFromHandThenMayCastOneEffect(new XValue()));
    }
}
