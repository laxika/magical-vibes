package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AirbendAllOtherCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsFromOutsideHandUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "12")
public class AvatarsWrath extends Card {

    public AvatarsWrath() {
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new AirbendAllOtherCreaturesEffect());
        addSpellEffect(new OpponentsCantCastSpellsFromOutsideHandUntilNextTurnEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
