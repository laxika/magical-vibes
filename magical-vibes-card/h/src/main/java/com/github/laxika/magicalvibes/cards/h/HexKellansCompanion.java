package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "14")
public class HexKellansCompanion extends Card {

    public HexKellansCompanion() {
        SpellCastTriggerEffect adventureTrigger = new SpellCastTriggerEffect(
                null,
                List.of(
                        new PerpetuallyBoostCardEffect(this, 1, 1),
                        new ConditionalEffect(new SourceIsOnBattlefield(),
                                new ExileSelfWithFetchCounterEffect())
                ),
                null, null, new StackEntryCastWithAdventurePredicate(), false, false,
                new AnyOf(List.of(new SourceCardOnBattlefield(), new SourceCardInExile())), 0);
        addTrigger(Triggers.youCastSpell(), adventureTrigger);
        addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.EXILE),
                TriggerOptions.DEFAULT, adventureTrigger);

        addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.EXILE), TriggerOptions.DEFAULT,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new ReturnSourceCardFromExileToBattlefieldEffect(false)),
                        null,
                        null,
                        new StackEntryCastFromZonePredicate(Zone.EXILE),
                        false,
                        false,
                        new SourceCardInExileWithFetchCounter(),
                        0));
    }
}
