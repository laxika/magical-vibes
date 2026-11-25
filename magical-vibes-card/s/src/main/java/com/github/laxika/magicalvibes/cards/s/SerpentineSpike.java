package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "133")
public class SerpentineSpike extends Card {

    public SerpentineSpike() {
        DealDamageEffect firstDamage = DealDamageEffect.toAnyTarget(new Fixed(2)).exilingDamagedCreaturesThatWouldDie();
        DealDamageEffect secondDamage = DealDamageEffect.toAnyTarget(new Fixed(3)).exilingDamagedCreaturesThatWouldDie();
        DealDamageEffect thirdDamage = DealDamageEffect.toAnyTarget(new Fixed(4)).exilingDamagedCreaturesThatWouldDie();
        target(TargetFilters.creature()).bindEffect(firstDamage);
        target(TargetFilters.creature()).bindEffect(secondDamage);
        target(TargetFilters.creature()).bindEffect(thirdDamage);
        addSimultaneousSpellEffects(firstDamage, secondDamage, thirdDamage);
    }
}
