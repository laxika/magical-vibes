package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "19")
public class CaseOfTheShiftingVisage extends Card {

    public CaseOfTheShiftingVisage() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(1));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new AllOf(List.of(
                        new GraveyardCardThreshold(15, null),
                        new NotCondition(new SourceIsSolved())
                )), new SolveSourceEffect()));

        CardAllOfPredicate nonlegendaryCreatureSpell = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY))
        ));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopyControllerCastSpellOnSpellCastEffect(
                        nonlegendaryCreatureSpell,
                        null,
                        null,
                        null,
                        null,
                        Set.of(),
                        new SourceIsSolved(),
                        false,
                        Set.of(),
                        true,
                        false,
                        false,
                        false,
                        null
                ));
    }
}
