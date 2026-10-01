package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YOTJ", collectorNumber = "7")
public class SolitaryDefiance extends Card {

    public SolitaryDefiance() {
        AllOf exactlyOneCreature = new AllOf(List.of(
                new ControlsPermanentCount(1, new PermanentIsCreaturePredicate()),
                new ControlsPermanentCountAtMost(1, new PermanentIsCreaturePredicate())));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(exactlyOneCreature,
                new GrantKeywordEffect(Set.of(Keyword.VIGILANCE, Keyword.WARD), GrantScope.OWN_CREATURES)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(exactlyOneCreature,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                        new CounterUnlessPaysEffect(2), GrantScope.OWN_CREATURES)));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new ConditionalEffect(new AttacksAlone(), SequenceEffect.of(
                        new SeekCardsToHandEffect(new Fixed(2),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
