package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetRevealsCardsControllerChoosesDiscardEffect;

@CardRegistration(set = "DST", collectorNumber = "50")
public class PulseOfTheDross extends Card {

    public PulseOfTheDross() {
        addEffect(EffectSlot.SPELL, new TargetRevealsCardsControllerChoosesDiscardEffect(3));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetPlayerHasMoreCardsInHandThanController(), ReturnToHandEffect.selfSpell()));
    }
}
