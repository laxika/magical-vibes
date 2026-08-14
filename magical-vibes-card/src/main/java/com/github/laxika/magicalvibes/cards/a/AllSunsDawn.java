package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneCardOfEachColorFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "5DN", collectorNumber = "81")
public class AllSunsDawn extends Card {

    public AllSunsDawn() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_COLOR);

        targetColor(CardColor.WHITE);
        targetColor(CardColor.BLUE);
        targetColor(CardColor.BLACK);
        targetColor(CardColor.RED);
        targetColor(CardColor.GREEN);

        addEffect(EffectSlot.SPELL, new ReturnUpToOneCardOfEachColorFromGraveyardToHandEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }

    private void targetColor(CardColor color) {
        target(new GraveyardCardPredicateTargetFilter(
                new CardColorPredicate(color), GraveyardSearchScope.CONTROLLERS_GRAVEYARD), 0, 1);
    }
}
