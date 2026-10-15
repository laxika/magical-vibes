package com.github.laxika.magicalvibes.cards.f;

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

@CardRegistration(set = "BNG", collectorNumber = "71")
public class ForlornPseudamma extends Card {

    public ForlornPseudamma() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new MayPayManaEffect("{2}{B}", new CreateTokenEffect(
                        "Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(CardType.ENCHANTMENT)),
                        "Pay {2}{B} to create a 2/2 black Zombie enchantment creature token?"));
    }
}
