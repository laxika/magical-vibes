package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "196")
public class InklingMascot extends Card {

    public InklingMascot() {
        // Repartee — Whenever you cast an instant or sorcery spell that targets a creature,
        // this creature gains flying until end of turn. Surveil 1.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate())),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                new SurveilEffect(1));
    }
}
