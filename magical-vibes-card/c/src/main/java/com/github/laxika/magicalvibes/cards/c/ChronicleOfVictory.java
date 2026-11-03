package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "253")
@CardRegistration(set = "ECL", collectorNumber = "346")
public class ChronicleOfVictory extends Card {

    public ChronicleOfVictory() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(new BoostCreaturesOfChosenSubtypeEffect(2, 2));
        addStaticEffect(new GrantKeywordEffect(
                Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSourceChosenSubtypePredicate()));
        addTrigger(Triggers.youCastSpell().withFilter(new CardHasSourceChosenSubtypePredicate(false)),
                new DrawCardEffect());
    }
}
