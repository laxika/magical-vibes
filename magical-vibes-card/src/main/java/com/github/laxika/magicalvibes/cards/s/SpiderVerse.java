package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleForControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "SPM", collectorNumber = "93")
@CardRegistration(set = "SPM", collectorNumber = "263")
public class SpiderVerse extends Card {

    public SpiderVerse() {
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleForControlledSubtypeEffect(CardSubtype.SPIDER));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, OncePerTurnTriggerEffect.markOnAcceptance(
                new MayEffect(
                        CopyControllerCastSpellOnSpellCastEffect.fromOutsideHandWithPermanentSpellHaste(null),
                        "Copy that spell?")));
    }
}
