package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "26")
@CardRegistration(set = "TMT", collectorNumber = "260")
public class TurncoatKunoichi extends Card {

    public TurncoatKunoichi() {
        addSneak("{2}{W}{B}");

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                        new NotCondition(new CastForAlternateCost()),
                        new ExileTargetPermanentUntilSourceLeavesEffect()),
                        new ConditionalEffect(
                        new CastForAlternateCost(), new ExileTargetPermanentEffect()));
    }
}
