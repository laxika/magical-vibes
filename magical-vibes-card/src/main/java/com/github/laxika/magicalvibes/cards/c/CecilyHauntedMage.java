package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseControllerMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SLX", collectorNumber = "3")
public class CecilyHauntedMage extends Card {

    public CecilyHauntedMage() {
        addEffect(EffectSlot.STATIC, new IncreaseControllerMaxHandSizeEffect(4));
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new DrawCardEffect(1),
                new LoseLifeEffect(1),
                ConditionalEffect.unless(new CardsInHandAtLeast(11),
                        new MayCastAnySpellFromHandWithoutPayingManaCostEffect())));
    }
}
