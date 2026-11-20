package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "286")
@CardRegistration(set = "SLD", collectorNumber = "1326")
@CardRegistration(set = "MAR", collectorNumber = "83")
public class SteelyResolve extends Card {

    public SteelyResolve() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new GrantKeywordEffect(
                Keyword.SHROUD,
                Subjects.allCreatures(),
                new PermanentHasSourceChosenSubtypePredicate()));
    }
}
