package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceTokenSubtypeCreationEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLC", collectorNumber = "36")
public class FishersTalent extends Card {

    public FishersTalent() {
        CreateTokenEffect fishToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Fish", 1, 1,
                CardColor.BLUE, null, List.of(CardSubtype.FISH), Set.of(), Set.of(),
                false, false, java.util.Map.of(), List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                ConditionalEffect.unless(
                        new TopCardOfLibraryType(CardType.LAND, LibraryOwner.CONTROLLER),
                        new MayEffect(
                                SequenceEffect.of(
                                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                                        fishToken),
                                "Reveal the land card?")),
                new DrawCardEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{U}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addEffect(EffectSlot.STATIC, new ReplaceTokenSubtypeCreationEffect(
                CardSubtype.FISH,
                new CreateTokenEffect(
                        "Shark", 3, 3, CardColor.BLUE, List.of(CardSubtype.SHARK), Set.of(), Set.of()),
                1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{U}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addEffect(EffectSlot.STATIC, new ReplaceTokenSubtypeCreationEffect(
                CardSubtype.SHARK,
                new CreateTokenEffect(
                        "Octopus", 8, 8, CardColor.BLUE, List.of(CardSubtype.OCTOPUS), Set.of(), Set.of()),
                2));
    }
}
