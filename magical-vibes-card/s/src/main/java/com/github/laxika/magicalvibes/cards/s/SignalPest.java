package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "131")
public class SignalPest extends Card {

    public SignalPest() {
        // Battle cry is auto-loaded from Scryfall and engine-handled.
        // Can't be blocked except by creatures with flying or reach.
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentHasKeywordPredicate(Keyword.REACH)
                )),
                "creatures with flying or reach"
        ));
    }
}
