package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "182")
public class ReptilDinomorpher extends Card {

    private static final List<CardSubtype> DINOSAUR_HERO = List.of(CardSubtype.DINOSAUR, CardSubtype.HERO);

    public ReptilDinomorpher() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(
                new SourceBecomesSubtypeUntilEndOfTurnEffect(DINOSAUR_HERO),
                new SetBasePowerToughnessEffect(3, 5, GrantScope.SELF),
                new GrantKeywordEffect(Set.of(Keyword.REACH, Keyword.VIGILANCE), GrantScope.SELF)),
                "Brontosaurus — {3}: Until end of turn, this creature becomes a Dinosaur Hero with base power and toughness 3/5 and gains reach and vigilance."));

        addActivatedAbility(new ActivatedAbility(false, "{6}", List.of(
                new SourceBecomesSubtypeUntilEndOfTurnEffect(DINOSAUR_HERO),
                new SetBasePowerToughnessEffect(6, 6, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)),
                "Tyrannosaurus Rex — {6}: Until end of turn, this creature becomes a Dinosaur Hero with base power and toughness 6/6 and gains trample."));
    }
}
