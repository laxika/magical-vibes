package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "XLN", collectorNumber = "82")
@CardRegistration(set = "MOC", collectorNumber = "237")
public class SpellSwindle extends Card {

    public SpellSwindle() {
        // Counter target spell. Create X Treasure tokens, where X is that spell's mana value. Countering records
        // the spell's mana value on the resolving entry, so the Treasures are made even if it can't be countered.
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(CreateTokenEffect.ofTreasureToken(new TargetSpellManaValue()));
    }
}
