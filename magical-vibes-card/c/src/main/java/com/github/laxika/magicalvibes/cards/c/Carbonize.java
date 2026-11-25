package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "SCG", collectorNumber = "83")
@CardRegistration(set = "EMA", collectorNumber = "122")
public class Carbonize extends Card {

    public Carbonize() {
        addSpellEffect(DealDamageEffect.toAnyTarget(new Fixed(3)).cantBeRegenerated().exilingDamagedCreaturesThatWouldDie()
                .withUnconditionalRegenerationPrevention());
    }
}
