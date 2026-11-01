package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "116")
public class BalduvianBerserker extends Card {

    public BalduvianBerserker() {
        // Enlist is loaded from Scryfall and handled during attacker declaration.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
