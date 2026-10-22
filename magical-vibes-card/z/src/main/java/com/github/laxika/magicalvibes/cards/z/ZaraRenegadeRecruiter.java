package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtDefendingPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "297")
public class ZaraRenegadeRecruiter extends Card {

    public ZaraRenegadeRecruiter() {
        // "Whenever Zara attacks, look at defending player's hand. You may put a creature card from it onto
        // the battlefield tapped and attacking." The look is not optional, so it precedes the "you may".
        addEffect(EffectSlot.ON_ATTACK, new SequenceEffect(List.of(
                new LookAtDefendingPlayerHandEffect(),
                new MayEffect(
                        ChooseCardFromTargetHandToBattlefieldEffect.forDefendingPlayerHand(
                                new CardTypePredicate(CardType.CREATURE), "creature"),
                        "Put a creature card from the defending player's hand onto the battlefield tapped and attacking?"))));
    }
}
