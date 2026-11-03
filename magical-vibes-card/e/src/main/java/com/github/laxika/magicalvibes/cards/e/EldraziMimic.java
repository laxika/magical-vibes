package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessFromEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "2")
public class EldraziMimic extends Card {

    public EldraziMimic() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsColorlessPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new SetSelfBasePowerToughnessFromEnteringCreatureEffect(),
                        "Have Eldrazi Mimic's base power and toughness become equal to that creature's power and toughness?"));
    }
}
