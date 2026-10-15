package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

/** Back face of Henrika Domnathi. */
public class HenrikaInfernalSeer extends Card {

    public HenrikaInfernalSeer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{B}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0, new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentHasKeywordPredicate(Keyword.DEATHTOUCH),
                        new PermanentHasKeywordPredicate(Keyword.LIFELINK))))),
                "{1}{B}{B}: Each creature you control with flying, deathtouch, and/or lifelink gets +1/+0 until end of turn."));
    }
}
