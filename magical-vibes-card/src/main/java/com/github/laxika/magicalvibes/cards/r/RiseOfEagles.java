package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "49")
public class RiseOfEagles extends Card {

    public RiseOfEagles() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Bird", 2, 2,
                CardColor.BLUE, List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING),
                Set.of(CardType.ENCHANTMENT)));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
