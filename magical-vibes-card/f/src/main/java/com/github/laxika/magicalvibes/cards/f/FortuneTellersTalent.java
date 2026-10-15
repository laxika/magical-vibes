package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceNonHandSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.EnumSet;
import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "14")
public class FortuneTellersTalent extends Card {

    private static final CardTruePredicate ANY_CARD = new CardTruePredicate();
    private static final EnumSet<CardType> SPELL_TYPES = EnumSet.of(
            CardType.CREATURE,
            CardType.ENCHANTMENT,
            CardType.SORCERY,
            CardType.INSTANT,
            CardType.ARTIFACT,
            CardType.PLANESWALKER,
            CardType.BATTLE,
            CardType.KINDRED
    );

    public FortuneTellersTalent() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new ControllerCastSpellThisTurn(ANY_CARD))),
                new PlayLandsFromTopOfLibraryEffect()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new ControllerCastSpellThisTurn(ANY_CARD))),
                new AllowCastFromTopOfLibraryEffect(SPELL_TYPES)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new ReduceNonHandSpellCastCostEffect(2)));
    }
}
