package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "93")
@CardRegistration(set = "SPM", collectorNumber = "263")
@CardRegistration(set = "OM1", collectorNumber = "75")
public class SpiderVerse extends Card {

    public SpiderVerse() {
        addStaticEffect(new IgnoreLegendRuleForControlledSubtypeEffect(CardSubtype.SPIDER));
        addTrigger(Triggers.youCastSpell(), OncePerTurnTriggerEffect.markOnAcceptance(
                new MayEffect(
                        CopyControllerCastSpellOnSpellCastEffect.fromOutsideHandWithPermanentSpellHaste(null),
                        "Copy that spell?")));
    }
}
