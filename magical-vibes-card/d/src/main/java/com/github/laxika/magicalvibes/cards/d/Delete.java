package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "WHO", collectorNumber = "81")
@CardRegistration(set = "WHO", collectorNumber = "381")
@CardRegistration(set = "WHO", collectorNumber = "686")
@CardRegistration(set = "WHO", collectorNumber = "972")
public class Delete extends Card {

    public Delete() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new XValue(), true, false,
                new PermanentNotPredicate(new PermanentIsArtifactPredicate())));
    }
}
