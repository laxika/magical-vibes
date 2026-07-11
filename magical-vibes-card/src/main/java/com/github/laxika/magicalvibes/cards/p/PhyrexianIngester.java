package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreaturePower;
import com.github.laxika.magicalvibes.model.amount.ImprintedCreatureToughness;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "41")
public class PhyrexianIngester extends Card {

    public PhyrexianIngester() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                )),
                "Target must be a nontoken creature"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                        new ExileTargetPermanentAndImprintEffect(),
                        "Exile target nontoken creature?"
                ));
        // Phyrexian Ingester gets +X/+Y, where X is the exiled creature card's power
        // and Y is its toughness.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new ImprintedCreaturePower(), new ImprintedCreatureToughness()));
    }
}
