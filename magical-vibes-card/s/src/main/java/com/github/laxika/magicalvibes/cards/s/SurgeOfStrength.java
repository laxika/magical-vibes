package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "109")
public class SurgeOfStrength extends Card {

    public SurgeOfStrength() {
        addCastingCost(new DiscardCardTypeCost(
                new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.RED),
                        new CardColorPredicate(CardColor.GREEN))),
                "red or green"));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.target()));
        addSpellEffect(BoostEffect.target(new TargetManaValue(), new Fixed(0)));
    }
}
