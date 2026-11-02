package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "41")
public class Mirozel extends Card {

    public Mirozel() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), ReturnToHandEffect.self());
    }
}
