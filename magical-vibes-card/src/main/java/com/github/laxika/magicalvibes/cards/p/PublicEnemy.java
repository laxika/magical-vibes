package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesMustAttackEnchantedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "55")
public class PublicEnemy extends Card {

    public PublicEnemy() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CreaturesMustAttackEnchantedCreatureControllerEffect());
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new DrawCardEffect());
    }
}
