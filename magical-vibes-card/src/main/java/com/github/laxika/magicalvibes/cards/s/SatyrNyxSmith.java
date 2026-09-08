package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "109")
public class SatyrNyxSmith extends Card {

    public SatyrNyxSmith() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MayPayManaEffect("{2}{R}", new CreateTokenEffect(
                        "Elemental", 3, 1, CardColor.RED,
                        List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), Set.of(CardType.ENCHANTMENT)),
                        "Pay {2}{R} to create a 3/1 red Elemental enchantment creature token with haste?"));
    }
}
