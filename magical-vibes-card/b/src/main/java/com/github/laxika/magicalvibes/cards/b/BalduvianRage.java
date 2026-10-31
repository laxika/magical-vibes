package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CSP", collectorNumber = "76")
@CardRegistration(set = "2XM", collectorNumber = "115")
public class BalduvianRage extends Card {

    public BalduvianRage() {
        target(TargetFilters.attackingCreature())
                .addSpellEffect(new BoostTargetCreatureEffect(new XValue(), new Fixed(0)));
        addSpellEffect(new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
