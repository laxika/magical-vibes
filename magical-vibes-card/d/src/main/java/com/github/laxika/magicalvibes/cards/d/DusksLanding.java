package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "10")
public class DusksLanding extends Card {

    public DusksLanding() {
        AllConditions seekCondition = new AllConditions(List.of(
                new OpponentLostLifeThisTurn(1),
                new GainedLifeThisTurn()
        ));
        addSpellEffect(new ConditionalEffect(seekCondition,
                new SeekLibraryEffect(2, new CardSubtypePredicate(CardSubtype.VAMPIRE))));
        addSpellEffect(new ConditionalEffect(new NotCondition(seekCondition),
                new DrawCardEffect(1)));
    }
}
