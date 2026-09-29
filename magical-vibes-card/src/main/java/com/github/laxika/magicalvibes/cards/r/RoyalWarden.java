package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "52")
public class RoyalWarden extends Card {

    public RoyalWarden() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Necron Warrior", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.NECRON, CardSubtype.WARRIOR), Set.of(),
                        Set.of(CardType.ARTIFACT), true));
        addUnearth("{3}{B}");
    }
}
