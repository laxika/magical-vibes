package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "66")
public class TriarchPraetorian extends Card {

    public TriarchPraetorian() {
        addTrigger(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(2),
                new LoseLifeEffect(2)));

        addUnearth("{4}{B}");
    }
}
