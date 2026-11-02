package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "34")
@CardRegistration(set = "HOC", collectorNumber = "74")
public class LothoCorruptShirriff extends Card {

    public LothoCorruptShirriff() {
        addTrigger(Triggers.playerCastsSpell(), new NthSpellCastTriggerEffect(
                2,
                List.of(new LoseLifeEffect(1), CreateTokenEffect.ofTreasureToken(1))
        ));
    }
}
