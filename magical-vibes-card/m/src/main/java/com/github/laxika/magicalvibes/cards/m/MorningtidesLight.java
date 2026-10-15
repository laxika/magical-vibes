package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ECL", collectorNumber = "27")
@CardRegistration(set = "ECL", collectorNumber = "301")
public class MorningtidesLight extends Card {

    public MorningtidesLight() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures"
        ), 0, 99)
                .addEffect(EffectSlot.SPELL, FlickerEffect.exileTargetReturnAtEndStep(true))
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allToControllerUntilNextTurn())
                .addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
