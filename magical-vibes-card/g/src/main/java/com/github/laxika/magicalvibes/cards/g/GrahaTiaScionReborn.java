package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "3")
@CardRegistration(set = "FIC", collectorNumber = "172")
@CardRegistration(set = "FIC", collectorNumber = "203")
@CardRegistration(set = "FIC", collectorNumber = "211")
@CardRegistration(set = "FIC", collectorNumber = "222")
public class GrahaTiaScionReborn extends Card {

    public GrahaTiaScionReborn() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayPayLifeEqualToAmountEffect(
                        new TargetSpellManaValue(), heroToken(),
                        "Pay life equal to that spell's mana value to create a Hero token?"));
    }

    private static CreateXTokenWithXCountersEffect heroToken() {
        return new CreateXTokenWithXCountersEffect(
                new CreateTokenEffect("Hero", 1, 1, null,
                        List.of(CardSubtype.HERO), Set.of(), Set.of()),
                new TargetSpellManaValue(),
                CounterType.PLUS_ONE_PLUS_ONE);
    }
}
