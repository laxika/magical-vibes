package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SCG", collectorNumber = "121")
public class HuntingPack extends Card {

    public HuntingPack() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Beast", 4, 4, CardColor.GREEN, List.of(CardSubtype.BEAST), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
