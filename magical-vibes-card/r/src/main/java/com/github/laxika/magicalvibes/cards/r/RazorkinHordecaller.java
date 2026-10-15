package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "152")
public class RazorkinHordecaller extends Card {

    public RazorkinHordecaller() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new CreateTokenEffect(
                "Gremlin", 1, 1, CardColor.RED, List.of(CardSubtype.GREMLIN), Set.of(), Set.of()));
    }
}
