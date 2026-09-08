package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "131")
public class SekKuarDeathkeeper extends Card {

    private static final CreateTokenEffect CREATE_GRAVEBORN = new CreateTokenEffect(
            1,
            "Graveborn",
            3,
            1,
            CardColor.BLACK,
            Set.of(CardColor.BLACK, CardColor.RED),
            List.of(CardSubtype.GRAVEBORN),
            Set.of(Keyword.HASTE),
            Set.of());

    public SekKuarDeathkeeper() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, CREATE_GRAVEBORN);
    }
}
