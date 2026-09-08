package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "1")
public class AdmonitionAngel extends Card {

    private static final PermanentAllOfPredicate TARGET = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentIsLandPredicate()),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
    ));

    public AdmonitionAngel() {
        target(new PermanentPredicateTargetFilter(TARGET,
                "Target must be another nonland permanent"))
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new MayEffect(new ExileTargetPermanentAndTrackWithSourceEffect(),
                                "Exile target nonland permanent other than Admonition Angel?"));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceEffect());
    }
}
