package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect;

import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "193")
public class SelflessSafewright extends Card {

    public SelflessSafewright() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantKeywordsToOwnPermanentsOfChosenSubtypeUntilEndOfTurnEffect(
                        Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE)));
    }
}
