package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "88")
public class LostLegacy extends Card {

    public LostLegacy() {
        addEffect(EffectSlot.SPELL,
                new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.ARTIFACT, CardType.LAND), null, true));
    }
}
