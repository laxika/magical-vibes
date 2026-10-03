package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DSC", collectorNumber = "359")
public class YouAreUnworthyOfMercy extends Card {

    public YouAreUnworthyOfMercy() {
        ControlsPermanentCount sixLands = new ControlsPermanentCount(6, new PermanentIsLandPredicate());
        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());

        addEffect(EffectSlot.SPELL, new ConditionalEffect(sixLands,
                new SacrificePermanentsEffect(3, nonland, SacrificeRecipient.EACH_OPPONENT)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(sixLands),
                new SacrificePermanentsEffect(1, nonland, SacrificeRecipient.EACH_OPPONENT)));
    }
}
