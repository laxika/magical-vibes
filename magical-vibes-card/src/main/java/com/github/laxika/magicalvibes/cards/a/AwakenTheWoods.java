package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "170")
public class AwakenTheWoods extends Card {

    public AwakenTheWoods() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Forest Dryad", 1, 1,
                CardColor.GREEN, null, List.of(CardSubtype.DRYAD), Set.of(), Set.of(CardType.LAND),
                false, false, Map.of(), List.of(), false, false, false, 0, Set.of()));
    }
}
