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

@CardRegistration(set = "MH1", collectorNumber = "59")
public class MoonbladeShinobi extends Card {

    public MoonbladeShinobi() {
        // Ninjutsu {2}{U}
        addNinjutsu("{2}{U}");

        // Whenever this creature deals combat damage to a player, create a 1/1 blue Illusion
        // creature token with flying.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenEffect("Illusion", 1, 1, CardColor.BLUE,
                        List.of(CardSubtype.ILLUSION), Set.of(Keyword.FLYING), Set.of()));
    }
}
