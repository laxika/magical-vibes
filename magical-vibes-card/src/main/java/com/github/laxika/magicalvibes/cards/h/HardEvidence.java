package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "46")
public class HardEvidence extends Card {

    public HardEvidence() {
        addEffect(EffectSlot.SPELL,
                new CreateTokenEffect("Crab", 0, 3, CardColor.BLUE, List.of(CardSubtype.CRAB),
                        Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
    }
}
