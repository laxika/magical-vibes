package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MayCastPermanentSpellFromHandOrPutLandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "213")
public class KellanTheKid extends Card {

    public KellanTheKid() {
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(
                        null,
                        List.of(new MayCastPermanentSpellFromHandOrPutLandEffect()),
                        new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND))));
    }
}
