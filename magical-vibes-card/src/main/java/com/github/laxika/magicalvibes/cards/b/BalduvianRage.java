package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "76")
public class BalduvianRage extends Card {

    public BalduvianRage() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new XValue(), new Fixed(0)));
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
