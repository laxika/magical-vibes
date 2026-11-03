package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExile;
import com.github.laxika.magicalvibes.model.condition.SourceCardInExileWithFetchCounter;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfWithFetchCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "14")
public class HexKellansCompanion extends Card {

    public HexKellansCompanion() {
        // One ability, functioning on the battlefield and in exile (CR 113.6k).
        SpellTrigger adventureSpell = Triggers.youCastSpell()
                .withStackEntryFilter(new StackEntryCastWithAdventurePredicate());
        addTriggeredAbility(new TriggeredAbility(
                List.of(new TriggerCondition(adventureSpell), new TriggerCondition(adventureSpell, AbilityZones.EXILE)),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyOf(List.of(new SourceCardOnBattlefield(), new SourceCardInExile()))),
                List.of(new EffectRegistration(new PerpetuallyBoostCardEffect(this, 1, 1)),
                        new EffectRegistration(new ConditionalEffect(new SourceIsOnBattlefield(),
                                new ExileSelfWithFetchCounterEffect())))));

        addTrigger(new TriggerCondition(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)), AbilityZones.EXILE),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExileWithFetchCounter()),
                new ReturnSourceCardFromExileToBattlefieldEffect(false));
    }
}
