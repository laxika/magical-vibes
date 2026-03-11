package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "96")
public class Duress extends Card {

    public Duress() {
        addEffect(EffectSlot.SPELL, new ChooseCardFromTargetHandToDiscardEffect(1, List.of(CardType.CREATURE, CardType.LAND)));
    }
}
