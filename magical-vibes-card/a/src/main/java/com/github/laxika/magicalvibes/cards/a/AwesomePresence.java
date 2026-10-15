package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatTaxKind;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCombatTaxEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALL", collectorNumber = "23a")
@CardRegistration(set = "ALL", collectorNumber = "23b")
public class AwesomePresence extends Card {

    public AwesomePresence() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCombatTaxEffect(3, CombatTaxKind.BE_BLOCKED_BY));
    }
}
