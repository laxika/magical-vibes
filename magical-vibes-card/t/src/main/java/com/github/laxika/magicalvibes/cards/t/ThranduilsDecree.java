package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "56")
public class ThranduilsDecree extends Card {

    public ThranduilsDecree() {
        // Counter target spell. If a permanent spell is countered this way, exile it instead of putting it into its
        // owner's graveyard. You may cast that card without paying its mana cost for as long as it remains exiled.
        addSpellEffect(new ConditionalReplacementEffect(
                new TargetSpellMatches(new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.ARTIFACT_SPELL,
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.PLANESWALKER_SPELL,
                        StackEntryType.BATTLE_SPELL))),
                new CounterSpellEffect(),
                SequenceEffect.of(
                        new CounterSpellEffect(CounteredSpellDestination.EXILE),
                        AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.COUNTER_SPELL))
                                .withoutPayingManaCost())));
    }
}
