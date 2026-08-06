package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "180")
public class MysticGenesis extends Card {

    public MysticGenesis() {
        // Counter target spell. Create an X/X green Ooze creature token, where X is that spell's
        // mana value.
        //
        // The token is listed before the counter so the targeted spell is still on the stack when
        // its mana value is snapshotted. The two instructions are independent, so this ordering is
        // rules-equivalent (the token is created at its size even for an uncounterable spell).
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Ooze",
                new TargetSpellManaValue(),
                new TargetSpellManaValue(),
                CardColor.GREEN,
                List.of(CardSubtype.OOZE),
                Set.of(),
                Set.of()));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
