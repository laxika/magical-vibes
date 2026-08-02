package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "126")
public class MimingSlime extends Card {

    public MimingSlime() {
        // Create an X/X green Ooze creature token, where X is the greatest power among creatures you control.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Ooze",
                new GreatestPowerAmongControlled(),
                new GreatestPowerAmongControlled(),
                CardColor.GREEN,
                List.of(CardSubtype.OOZE),
                Set.of(),
                Set.of()));
    }
}
