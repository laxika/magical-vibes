package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "66")
public class GruesomeDiscovery extends Card {

    public GruesomeDiscovery() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Morbid(), 
                new TargetPlayerDiscardsEffect(2),
                new ChooseCardFromTargetHandToDiscardEffect(2, List.of())
        ));
    }
}
