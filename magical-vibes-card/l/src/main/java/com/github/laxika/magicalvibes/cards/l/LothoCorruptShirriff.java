package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "34")
@CardRegistration(set = "HOC", collectorNumber = "74")
public class LothoCorruptShirriff extends Card {

    public LothoCorruptShirriff() {
        addTrigger(Triggers.playerCastsSpell().nthThisTurn(2),
                new LoseLifeEffect(1),
                CreateTokenEffect.ofTreasureToken(1));
    }
}
