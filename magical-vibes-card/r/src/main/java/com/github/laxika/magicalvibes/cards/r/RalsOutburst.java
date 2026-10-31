package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "WAR", collectorNumber = "212")
public class RalsOutburst extends Card {

    public RalsOutburst() {
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addSpellEffect(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1));
    }
}
