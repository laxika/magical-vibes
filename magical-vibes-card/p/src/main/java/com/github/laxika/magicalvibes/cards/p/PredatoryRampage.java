package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatureBlocksThisTurnIfAbleEffect;

@CardRegistration(set = "M13", collectorNumber = "180")
@CardRegistration(set = "CMM", collectorNumber = "313")
public class PredatoryRampage extends Card {

    public PredatoryRampage() {
        addSpellEffect(new BoostAllOwnCreaturesEffect(3, 3));
        addSpellEffect(new EachOpponentCreatureBlocksThisTurnIfAbleEffect());
    }
}
