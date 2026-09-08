package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "202")
public class ExplorersScope extends Card {

    public ExplorersScope() {
        addEffect(EffectSlot.ON_ATTACK,
                new LookAtTopCardMayPutMatchingOntoBattlefieldEffect(
                        new CardTypePredicate(CardType.LAND), true));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
