package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "72")
public class EmptyThePits extends Card {

    public EmptyThePits() {
        addEffect(EffectSlot.SPELL, new DelveCost());
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new XValue(), "Zombie", 2, 2,
                CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE),
                Set.of(), Set.of(),
                false, true,
                Map.of(), List.of(),
                false, false, false, 0, Set.of()));
    }
}
