package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;

@CardRegistration(set = "MID", collectorNumber = "31")
public class RitualOfHope extends Card {

    public RitualOfHope() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Coven(),
                new BoostAllOwnCreaturesEffect(1, 1),
                new BoostAllOwnCreaturesEffect(2, 1)));
    }
}
