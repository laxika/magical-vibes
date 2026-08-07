package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatTaxKind;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCombatTaxEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "13")
@CardRegistration(set = "4ED", collectorNumber = "11")
public class Brainwash extends Card {

    public Brainwash() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCombatTaxEffect(3, CombatTaxKind.ATTACK));
    }
}
