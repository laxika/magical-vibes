package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;

@CardRegistration(set = "MB1", collectorNumber = "174")
@CardRegistration(set = "SS2", collectorNumber = "2")
public class MartyrsBond extends Card {

    public MartyrsBond() {
        SacrificePermanentsEffect sacrificeMatchingType = new SacrificePermanentsEffect(
                1,
                new PermanentSharesCardTypeWithSourcePermanentPredicate(),
                SacrificeRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, sacrificeMatchingType);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()), sacrificeMatchingType));
    }
}
