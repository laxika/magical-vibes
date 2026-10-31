package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CombatTaxKind;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCombatTaxEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOK", collectorNumber = "5")
public class CowedByWisdom extends Card {

    public CowedByWisdom() {
        target(TargetFilters.creature())
                .addStaticEffect(new EnchantedCreatureCombatTaxEffect(
                        new CardsInHand(CountScope.CONTROLLER), CombatTaxKind.ATTACK))
                .addStaticEffect(new EnchantedCreatureCombatTaxEffect(
                        new CardsInHand(CountScope.CONTROLLER), CombatTaxKind.BLOCK_WITH));
    }
}
