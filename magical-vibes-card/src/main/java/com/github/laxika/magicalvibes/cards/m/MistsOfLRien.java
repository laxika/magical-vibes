package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentAndAllWithSameManaValueToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "501")
@CardRegistration(set = "LTC", collectorNumber = "545")
public class MistsOfLRien extends Card {

    public MistsOfLRien() {
        PermanentNotPredicate nonlandPermanent = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{U}")));
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL,
                        new ReturnTargetPermanentAndAllWithSameManaValueToHandEffect(
                                nonlandPermanent, nonlandPermanent));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{U}"));
    }
}
