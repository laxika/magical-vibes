package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerToughnessEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "220")
public class RoarOfEndlessSong extends Card {

    public RoarOfEndlessSong() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(
                "Elephant", 5, 5, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                "Elephant", 5, 5, CardColor.GREEN, List.of(CardSubtype.ELEPHANT), Set.of(), Set.of()));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new DoubleAllOwnCreaturesPowerToughnessEffect());
    }
}
