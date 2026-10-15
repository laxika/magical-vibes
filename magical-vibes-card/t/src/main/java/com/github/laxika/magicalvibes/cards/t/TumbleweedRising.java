package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "187")
public class TumbleweedRising extends Card {

    public TumbleweedRising() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{G}"))));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Elemental",
                new GreatestPowerAmongControlled(),
                new GreatestPowerAmongControlled(),
                CardColor.GREEN,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(),
                Set.of()));
    }
}
