package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSplitSecondToControllerSpellsUsingArtifactManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2086")
public class ShadowTheHedgehog extends Card {

    public ShadowTheHedgehog() {
        addTrigger(Triggers.creatureDies(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasKeywordPredicate(Keyword.FLASH),
                                new PermanentHasKeywordPredicate(Keyword.HASTE))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new DrawCardEffect(1));
        addStaticEffect(new GrantSplitSecondToControllerSpellsUsingArtifactManaEffect());
    }
}
