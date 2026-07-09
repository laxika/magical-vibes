package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsForControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;

@CardRegistration(set = "LRW", collectorNumber = "106")
public class ColfenorsPlans extends Card {

    public ColfenorsPlans() {
        // When this enchantment enters, exile the top seven cards of your library face down.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(7));
        // You may look at / play lands and cast spells from among the cards exiled with it.
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(false));
        // Skip your draw step.
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());
        // You can't cast more than one spell each turn.
        addEffect(EffectSlot.STATIC, new LimitSpellsForControllerEffect(1));
    }
}
