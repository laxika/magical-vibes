package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "71")
public class GruesomeMenagerie extends Card {

    public GruesomeMenagerie() {
        addEffect(EffectSlot.SPELL,
                new ReturnOneCreatureOfEachManaValueFromGraveyardToBattlefieldEffect(List.of(1, 2, 3)));
    }
}
