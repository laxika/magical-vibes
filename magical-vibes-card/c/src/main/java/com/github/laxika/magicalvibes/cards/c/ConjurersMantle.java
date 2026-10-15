package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "12")
@CardRegistration(set = "MOC", collectorNumber = "99")
public class ConjurersMantle extends Card {

    public ConjurersMantle() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ATTACK,
                new LookAtTopCardsCreatureSharingTypeWithEquippedToHandEffect(6));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
