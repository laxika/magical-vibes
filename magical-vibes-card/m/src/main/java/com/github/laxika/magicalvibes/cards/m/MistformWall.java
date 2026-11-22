package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import java.util.Set;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "99")
public class MistformWall extends Card {

    public MistformWall() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.DEFENDER, Subjects.source(), new PermanentHasSubtypePredicate(CardSubtype.WALL)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(Subjects.source(), Set.of())),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
    }
}
