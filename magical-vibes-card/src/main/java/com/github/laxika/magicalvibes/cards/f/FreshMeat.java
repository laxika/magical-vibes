package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerOwnCreatureDeathsThisTurnEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "109")
public class FreshMeat extends Card {

    public FreshMeat() {
        addEffect(EffectSlot.SPELL, new CreateTokensPerOwnCreatureDeathsThisTurnEffect(
                "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST)
        ));
    }
}
