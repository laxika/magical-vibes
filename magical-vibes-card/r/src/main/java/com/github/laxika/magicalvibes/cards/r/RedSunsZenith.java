package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "74")
public class RedSunsZenith extends Card {

    public RedSunsZenith() {
        // X damage; a creature dealt damage this way that would die this turn is exiled instead.
        addSpellEffect(new DealDamageToAnyTargetEffect(new XValue(), false, true));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
