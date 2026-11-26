package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Corvid Squall, the prepare spell of Galathul Galecaller // Corvid Squall (YSOS 3). */
public class CorvidSquall extends Card {

    public CorvidSquall() {
        addSpellEffect(ConjureCardsEffect.named("9ED", "100", ConjureDestination.ontoBattlefield()));
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
