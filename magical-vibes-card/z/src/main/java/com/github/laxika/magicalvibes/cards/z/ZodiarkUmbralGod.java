package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedDownEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "128")
@CardRegistration(set = "FIN", collectorNumber = "336")
@CardRegistration(set = "FIN", collectorNumber = "456")
public class ZodiarkUmbralGod extends Card {

    public ZodiarkUmbralGod() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerSacrificesFractionRoundedDownEffect(2,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GOD))))));
        addTrigger(Triggers.sacrificed(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
