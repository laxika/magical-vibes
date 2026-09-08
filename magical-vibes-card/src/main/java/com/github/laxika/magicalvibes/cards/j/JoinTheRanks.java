package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "9")
public class JoinTheRanks extends Card {

    public JoinTheRanks() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Soldier Ally", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.SOLDIER, CardSubtype.ALLY), Set.of(), Set.of()));
    }
}
