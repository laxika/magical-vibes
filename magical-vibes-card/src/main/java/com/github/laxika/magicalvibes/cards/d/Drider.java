package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "98")
public class Drider extends Card {

    public Drider() {
        // Whenever this creature deals combat damage to a player, create a 2/1 black Spider
        // creature token with reach and menace.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenEffect("Spider", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH, Keyword.MENACE), Set.of()));
    }
}
