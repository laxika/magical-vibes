package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BubbleUp;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "40")
public class AquaticAlchemistBubbleUp extends Card {

    public AquaticAlchemistBubbleUp() {
        setBackFaceCard(new BubbleUp());
        addCastingOption(new AdventureCast("{2}{U}"));
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .nthThisTurn(1),
                new BoostSelfEffect(2, 0));
    }

    @Override
    public String getBackFaceClassName() {
        return "BubbleUp";
    }
}
