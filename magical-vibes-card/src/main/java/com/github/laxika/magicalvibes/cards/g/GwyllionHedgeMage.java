package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "89")
public class GwyllionHedgeMage extends Card {

    public GwyllionHedgeMage() {
        // When this creature enters, if you control two or more Plains, you may create a 1/1 white
        // Kithkin Soldier creature token. Intervening-if gate (CR 603.4): checked as the trigger
        // goes on the stack and again at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.PLAINS)),
                new MayEffect(new CreateTokenEffect("Kithkin Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER), Set.of(), Set.of()),
                        "Create a 1/1 white Kithkin Soldier creature token?")));

        // When this creature enters, if you control two or more Swamps, you may put a -1/-1 counter
        // on target creature. The gate defers targeting to trigger time (CR 603.3d) — target is a
        // creature (default ETB permanent-target legality); the "you may" is decided at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                new MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        "Put a -1/-1 counter on target creature?")));
    }
}
