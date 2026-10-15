package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentEndStep;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;

@CardRegistration(set = "MKC", collectorNumber = "22")
@CardRegistration(set = "MKC", collectorNumber = "332")
public class FinalWordPhantom extends Card {

    public FinalWordPhantom() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentEndStep(),
                new GrantFlashToCardTypeEffect(null)));
    }
}
