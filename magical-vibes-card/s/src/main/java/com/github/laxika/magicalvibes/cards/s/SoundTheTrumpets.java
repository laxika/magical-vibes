package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "55")
public class SoundTheTrumpets extends Card {

    public SoundTheTrumpets() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new ConditionalEffect(
                new TargetSpellMatches(new StackEntryMaxManaValuePredicate(2)),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        SequenceEffect.of(
                                new DiscardEffect(1, Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.toACard(StepKind.DISCARD, new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                                        new CreateTokenEffect(CardType.CREATURE, 1, "Soldier", 1, 1, CardColor.WHITE, null,
                                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of(),
                                                false, false, List.of(), List.of(), false, false, false, 0, Set.of()))))));
    }
}
