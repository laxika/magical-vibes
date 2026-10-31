package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "P02", collectorNumber = "69")
@CardRegistration(set = "S99", collectorNumber = "72")
@CardRegistration(set = "ME4", collectorNumber = "73")
public class DakmorPlague extends Card {

    public DakmorPlague() {
        addSpellEffect(new MassDamageEffect(3, true));
    }
}
