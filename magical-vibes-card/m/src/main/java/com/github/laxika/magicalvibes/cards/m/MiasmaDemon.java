package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "109")
public class MiasmaDemon extends Card {

    public MiasmaDemon() {
        targetX(TargetFilters.creature(), 100);
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new DiscardAnyNumberThenEffect(
                        new CardTruePredicate(),
                        BoostTargetCreatureEffect.forTargetGroup(-2, -2, 0),
                        "cards"),
                "Discard any number of cards?"));
    }
}
