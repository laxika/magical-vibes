package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerCreatureControlledEffect;

@CardRegistration(set = "10E", collectorNumber = "181")
@CardRegistration(set = "NEM", collectorNumber = "73")
public class StrongholdDiscipline extends Card {

    public StrongholdDiscipline() {
        addSpellEffect(new EachPlayerLosesLifePerCreatureControlledEffect(1, false));
    }
}
