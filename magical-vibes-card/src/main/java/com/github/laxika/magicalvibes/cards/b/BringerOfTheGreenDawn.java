package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "83")
public class BringerOfTheGreenDawn extends Card {

    public BringerOfTheGreenDawn() {
        // You may pay {W}{U}{B}{R}{G} rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{W}{U}{B}{R}{G}"))));

        // At the beginning of your upkeep, you may create a 3/3 green Beast creature token.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.BEAST), Set.of(), Set.of()),
                "Create a 3/3 green Beast creature token?"
        ));
    }
}
