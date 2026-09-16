package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSharingDyingPermanentTypeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MB1", collectorNumber = "174")
@CardRegistration(set = "SS2", collectorNumber = "2")
public class MartyrsBond extends Card {

    public MartyrsBond() {
        addEffect(EffectSlot.ON_DEATH, new SacrificePermanentsSharingDyingPermanentTypeEffect());
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new SacrificePermanentsSharingDyingPermanentTypeEffect()));
    }
}
