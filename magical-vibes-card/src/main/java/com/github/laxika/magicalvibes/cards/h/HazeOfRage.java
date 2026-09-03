package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.BuybackPaid;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BuybackEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "FUT", collectorNumber = "100")
public class HazeOfRage extends Card {

    public HazeOfRage() {
        addEffect(EffectSlot.STATIC, new BuybackEffect("{2}"));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell()));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
