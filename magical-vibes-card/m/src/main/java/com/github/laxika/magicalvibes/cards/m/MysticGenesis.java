package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "180")
@CardRegistration(set = "MM3", collectorNumber = "174")
public class MysticGenesis extends Card {

    public MysticGenesis() {
        // Counter target spell. Create an X/X green Ooze creature token, where X is that spell's
        // mana value. Countering records the spell's mana value on the resolving entry, so the token
        // is still created at that size after the spell has left the stack (even if it was uncounterable).
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new CreateTokenEffect(
                "Ooze",
                new TargetSpellManaValue(),
                new TargetSpellManaValue(),
                CardColor.GREEN,
                List.of(CardSubtype.OOZE),
                Set.of(),
                Set.of()));
    }
}
