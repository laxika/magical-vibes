package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;

@CardRegistration(set = "MH1", collectorNumber = "154")
public class VolatileClaws extends Card {

    public VolatileClaws() {
        // Creatures you control get +2/+0 and gain all creature types until end of turn.
        addSpellEffect(new BoostAllOwnCreaturesEffect(2, 0));
        addSpellEffect(new GrantAllCreatureTypesToOwnCreaturesEffect());
    }
}
