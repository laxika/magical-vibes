package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BRO", collectorNumber = "189")
public class SarinthSteelseeker extends Card {

    public SarinthSteelseeker() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new LookAtTopCardMayRevealMatchingToHandEffect(
                        new CardTypePredicate(CardType.LAND), true));
    }
}
