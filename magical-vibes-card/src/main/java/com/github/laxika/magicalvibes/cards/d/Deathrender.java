package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "255")
public class Deathrender extends Card {

    public Deathrender() {
        // Equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature dies, you may put a creature card from your hand onto the
        // battlefield and attach this Equipment to it. The card choice is declinable ("you may").
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new PutCardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), "creature", false, false, false, false, true));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
