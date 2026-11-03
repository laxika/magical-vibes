package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "30")
@CardRegistration(set = "BLC", collectorNumber = "64")
public class EvercoatUrsine extends Card {

    public EvercoatUrsine() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new ExileOneFromTopCardsFaceDownWithSourceEffect(3, true),
                new ExileOneFromTopCardsFaceDownWithSourceEffect(3, true)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceExiledCardsThreshold(1)),
                new MayPlayCardExiledWithSourceEffect());
    }
}
