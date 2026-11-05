package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "231")
@CardRegistration(set = "AFC", collectorNumber = "191")
@CardRegistration(set = "C17", collectorNumber = "191")
@CardRegistration(set = "SCD", collectorNumber = "240")
public class SavageVentmaw extends Card {

    public SavageVentmaw() {
        addTrigger(Triggers.attacks(),
                new AwardPersistentManaEffect(ManaColor.RED, new Fixed(3)),
                new AwardPersistentManaEffect(ManaColor.GREEN, new Fixed(3)));
    }
}
