package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "11")
public class EmeriaAngel extends Card {

    public EmeriaAngel() {
        // Landfall - Whenever a land you control enters, you may create a 1/1 white Bird creature token with flying.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new MayEffect(
                new CreateTokenEffect("Bird", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()),
                "Create a 1/1 white Bird creature token with flying?"));
    }
}
