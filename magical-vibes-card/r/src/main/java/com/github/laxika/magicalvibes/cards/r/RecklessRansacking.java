package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "ECL", collectorNumber = "152")
public class RecklessRansacking extends Card {

    public RecklessRansacking() {
        addSpellEffect(new BoostTargetCreatureEffect(3, 2));
        addSpellEffect(CreateTokenEffect.ofTreasureToken(1));
    }
}
