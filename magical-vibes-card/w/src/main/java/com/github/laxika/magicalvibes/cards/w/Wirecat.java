package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "USG", collectorNumber = "317")
public class Wirecat extends Card {

    public Wirecat() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new NotCondition(new AnyPlayerControlsPermanent(new PermanentIsEnchantmentPredicate())),
                "there is no enchantment on the battlefield"
        ));
    }
}
