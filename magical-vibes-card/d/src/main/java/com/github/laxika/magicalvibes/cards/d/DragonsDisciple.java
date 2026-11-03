package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "13")
public class DragonsDisciple extends Card {

    public DragonsDisciple() {
        PermanentHasSubtypePredicate dragon = new PermanentHasSubtypePredicate(CardSubtype.DRAGON);

        addStaticEffect(new ConditionalEffect(
                new ControlsPermanent(dragon),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));

        addStaticEffect(new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0, dragon),
                new RevealSubtypeOrEntersWithCountersEffect(
                        CardSubtype.DRAGON, CounterType.PLUS_ONE_PLUS_ONE, 1)));

        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(1)),
                GrantScope.ALL_OWN_CREATURES,
                dragon));
    }
}
