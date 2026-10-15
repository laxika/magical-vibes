package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "200")
public class IronCrawCrusher extends Card {

    public IronCrawCrusher() {
        addPrototype("{2}{G}{G}", CardColor.GREEN, 2, 5);
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.ON_ATTACK,
                new BoostTargetCreatureEffect(new SourcePower(), new Fixed(0)));
    }
}
