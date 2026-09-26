package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithLessPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "37")
@CardRegistration(set = "LTC", collectorNumber = "120")
public class FeastingHobbit extends Card {

    public FeastingHobbit() {
        // Devour Food 3 (As this creature enters, you may sacrifice any number of Foods.
        // It enters with three times that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsAsEntersForCountersEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FOOD), 3));

        // Creatures with power less than this creature's power can't block it.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithLessPowerEffect());
    }
}
