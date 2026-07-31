package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "21")
public class HiveStirrings extends Card {

    public HiveStirrings() {
        // Create two 1/1 colorless Sliver creature tokens. A null color is the empty color set of CR 105.3.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Sliver", 1, 1, null,
                List.of(CardSubtype.SLIVER), Set.of(), Set.of()));
    }
}
