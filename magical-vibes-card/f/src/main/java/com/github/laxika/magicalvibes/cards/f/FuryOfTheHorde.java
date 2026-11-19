package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "81")
@CardRegistration(set = "DDN", collectorNumber = "30")
@CardRegistration(set = "MAR", collectorNumber = "71")
public class FuryOfTheHorde extends Card {

    public FuryOfTheHorde() {
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.RED), "red", 2))));

        addSpellEffect(new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                        new PermanentAttackedThisTurnPredicate())))));
        addSpellEffect(new AdditionalCombatMainPhaseEffect(1, null, true));
    }
}
