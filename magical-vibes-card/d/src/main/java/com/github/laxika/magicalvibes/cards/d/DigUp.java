package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "VOW", collectorNumber = "197")
@CardRegistration(set = "DBL", collectorNumber = "464")
public class DigUp extends Card {

    public DigUp() {
        addCastingOption(AlternateHandCast.cleave("{1}{B}{B}{G}", null));

        addSpellEffect(new ConditionalEffect(
                new CastForAlternateCost(), new SearchLibraryEffect()));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new SearchLibraryEffect(CardPredicateUtils.basicLand())));
    }
}
