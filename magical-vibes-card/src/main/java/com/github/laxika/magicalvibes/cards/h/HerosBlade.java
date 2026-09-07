package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

@CardRegistration(set = "FRF", collectorNumber = "160")
public class HerosBlade extends Card {

    public HerosBlade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
