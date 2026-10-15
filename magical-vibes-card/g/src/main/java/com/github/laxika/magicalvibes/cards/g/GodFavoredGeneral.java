package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "14")
public class GodFavoredGeneral extends Card {

    public GodFavoredGeneral() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MayPayManaEffect("{2}{W}", new CreateTokenEffect(
                        2, "Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ENCHANTMENT)),
                        "Pay {2}{W} to create two 1/1 white Soldier enchantment creature tokens?"));
    }
}
