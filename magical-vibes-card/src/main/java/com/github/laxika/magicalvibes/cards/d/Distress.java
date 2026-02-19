package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "136")
public class Distress extends Card {

    public Distress() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new ChooseCardFromTargetHandToDiscardEffect(1, List.of(CardType.LAND)));
    }
}
