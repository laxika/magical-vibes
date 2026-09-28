package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LCC", collectorNumber = "297")
public class ZaraRenegadeRecruiter extends Card {

    public ZaraRenegadeRecruiter() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                ChooseCardFromTargetHandToBattlefieldEffect.forDefendingPlayerHand(
                        new CardTypePredicate(CardType.CREATURE), "creature"),
                "Put a creature card from the defending player's hand onto the battlefield tapped and attacking?"));
    }
}
