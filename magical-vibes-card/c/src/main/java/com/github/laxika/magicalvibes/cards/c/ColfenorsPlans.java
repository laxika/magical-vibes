package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.SpellLimitScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "106")
public class ColfenorsPlans extends Card {

    public ColfenorsPlans() {
        // When this enchantment enters, exile the top seven cards of your library face down.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardsToSourceEffect(7));
        // You may look at / play lands and cast spells from among the cards exiled with it.
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(false));
        // Skip your draw step.
        addStaticEffect(new SkipDrawStepEffect());
        // You can't cast more than one spell each turn.
        addStaticEffect(new LimitSpellsPerTurnEffect(1, SpellLimitScope.CONTROLLER));
    }
}
