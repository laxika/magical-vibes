package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "79")
public class KurosTaken extends Card {

    public KurosTaken() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Kuros Taken."
        ));
    }
}
