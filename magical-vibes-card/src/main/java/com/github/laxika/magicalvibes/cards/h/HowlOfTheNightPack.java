package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledLandSubtypeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "187")
public class HowlOfTheNightPack extends Card {

    public HowlOfTheNightPack() {
        addEffect(EffectSlot.SPELL, new CreateTokensPerControlledLandSubtypeEffect(
                CardSubtype.FOREST,
                "Wolf", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.WOLF),
                Set.of(), Set.of()
        ));
    }
}
