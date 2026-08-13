package com.github.laxika.magicalvibes.cards.a;

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

@CardRegistration(set = "BNG", collectorNumber = "30")
public class AerieWorshippers extends Card {

    public AerieWorshippers() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MayPayManaEffect("{2}{U}", new CreateTokenEffect(
                        "Bird", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of(CardType.ENCHANTMENT)),
                        "Pay {2}{U} to create a 2/2 blue Bird enchantment creature token?"));
    }
}
