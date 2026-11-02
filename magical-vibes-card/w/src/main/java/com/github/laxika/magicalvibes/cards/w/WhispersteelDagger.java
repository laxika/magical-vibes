package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNC", collectorNumber = "5")
public class WhispersteelDagger extends Card {

    public WhispersteelDagger() {
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(),
                new AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect(
                        new CardTypePredicate(CardType.CREATURE), true));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
