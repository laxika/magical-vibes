package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "125")
public class SynapseNecromage extends Card {

    public SynapseNecromage() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                CardType.CREATURE, 2, "Fungus", 1, 1, CardColor.BLACK, null,
                List.of(CardSubtype.FUNGUS), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, new CantBlockEffect()), List.of(),
                false, false, false, 0, Set.of()
        ));
    }
}
