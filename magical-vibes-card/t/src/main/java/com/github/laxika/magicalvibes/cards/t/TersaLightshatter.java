package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromGraveyardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "127")
public class TersaLightshatter extends Card {

    public TersaLightshatter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardUpToThenDrawThatManyEffect(2));

        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new GraveyardCardThreshold(7, new CardTruePredicate()),
                new ExileRandomCardFromGraveyardMayPlayThisTurnEffect()));
    }
}
