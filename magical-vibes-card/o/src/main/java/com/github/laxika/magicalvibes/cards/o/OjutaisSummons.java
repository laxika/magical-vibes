package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "68")
public class OjutaisSummons extends Card {

    public OjutaisSummons() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Djinn Monk", 2, 2, CardColor.BLUE,
                List.of(CardSubtype.DJINN, CardSubtype.MONK), Set.of(Keyword.FLYING), Set.of()));
    }
}
