package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "539")
public class MinotaurRoxxonCEO extends Card {

    private static final CreateTokenEffect VILLAIN_TOKEN = new CreateTokenEffect(
            1, "Villain", 2, 1, CardColor.BLACK,
            List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of());

    public MinotaurRoxxonCEO() {
        // Whenever this creature or another nontoken creature dies, create a 2/1 black Villain
        // creature token with menace.
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES, VILLAIN_TOKEN);
        addEffect(EffectSlot.ON_DEATH, VILLAIN_TOKEN);
    }
}
