package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "54")
public class LullmageMentor extends Card {

    public LullmageMentor() {
        addEffect(EffectSlot.ON_CONTROLLER_COUNTERS_SPELL, new MayEffect(
                new CreateTokenEffect("Merfolk", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.MERFOLK), Set.of(), Set.of()),
                "Create a 1/1 blue Merfolk creature token?"));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(7, new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)),
                        new CounterSpellEffect()),
                "Tap seven untapped Merfolk you control: Counter target spell."));
    }
}
