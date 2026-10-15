package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMKM", collectorNumber = "4")
public class CaseOfTheLostWitness extends Card {

    public CaseOfTheLostWitness() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardNamedIntoLibraryEffect("Fblthp, the Lost", 4),
                new DrawCardEffect(1)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new AllOf(List.of(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                                new PermanentHasSubtypePredicate(CardSubtype.HOMUNCULUS)))),
                        new NotCondition(new SourceIsSolved())
                )), new SolveSourceEffect()));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(), new LookAtTopCardOfOwnLibraryEffect()));
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(), new PlayLandsFromTopOfLibraryEffect()));
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(new SourceIsSolved(), new AllowCastFromTopOfLibraryEffect(Set.of(
                        CardType.CREATURE,
                        CardType.ENCHANTMENT,
                        CardType.SORCERY,
                        CardType.INSTANT,
                        CardType.ARTIFACT,
                        CardType.PLANESWALKER,
                        CardType.BATTLE,
                        CardType.KINDRED
                ))));
    }
}
