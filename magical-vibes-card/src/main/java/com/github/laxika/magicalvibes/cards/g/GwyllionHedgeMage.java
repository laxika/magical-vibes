package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "89")
@CardRegistration(set = "CMD", collectorNumber = "202")
@CardRegistration(set = "CMA", collectorNumber = "201")
public class GwyllionHedgeMage extends Card {

    public GwyllionHedgeMage() {
        // Two separate "When this creature enters" abilities, so each is its own triggered ability
        // (TriggerMode.INDEPENDENT). Each intervening-if gate (CR 603.4) is checked as the trigger
        // goes on the stack and again at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                new MayEffect(new CreateTokenEffect("Kithkin Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER), Set.of(), Set.of()),
                        "Create a 1/1 white Kithkin Soldier creature token?")), TriggerMode.INDEPENDENT);

        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                new MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        "Put a -1/-1 counter on target creature?")), TriggerMode.INDEPENDENT);
    }
}
