package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "137")
public class FirebenderAscension extends Card {

    public FirebenderAscension() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE, 1, "Soldier", 2, 2,
                CardColor.RED, Set.of(), List.of(CardSubtype.SOLDIER),
                Set.of(Keyword.FIREBENDING), Set.of(), false, false,
                List.of(new TriggeredAbility(Triggers.attacks(),
                        new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1))),
                List.of(), false, false, false, 0, Set.of()));
        // Whenever a creature you control attacking causes a triggered ability of that creature to trigger, put a
        // quest counter on this enchantment. Then if it has four or more quest counters on it, you may copy that
        // ability. You may choose new targets for the copy.
        addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(),
                new PutCountersOnSourceCardEffect(CounterType.QUEST),
                new ConditionalEffect(new SourceCounterThreshold(4, CounterType.QUEST),
                        new MayEffect(new CopyTriggeringSpellOrAbilityEffect(), "Copy that triggered ability?")));
    }
}
