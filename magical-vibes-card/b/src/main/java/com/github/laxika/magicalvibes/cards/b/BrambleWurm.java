package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "187")
@CardRegistration(set = "INR", collectorNumber = "407")
@CardRegistration(set = "VOW", collectorNumber = "189")
@CardRegistration(set = "DBL", collectorNumber = "456")
public class BrambleWurm extends Card {

    public BrambleWurm() {
        // When this creature enters, you gain 5 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(5));

        // {2}{G}, Exile this card from your graveyard: You gain 5 life.
        addActivatedAbility(AbilityZones.GRAVEYARD, new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new GainLifeEffect(5)
                ),
                "{2}{G}, Exile this card from your graveyard: You gain 5 life."
        ));
    }
}
