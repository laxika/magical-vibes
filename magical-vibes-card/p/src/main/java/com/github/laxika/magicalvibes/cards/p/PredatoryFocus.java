package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "GPT", collectorNumber = "92")
public class PredatoryFocus extends Card {

    public PredatoryFocus() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect(
                        new AssignCombatDamageAsThoughUnblockedEffect(true)),
                "Have your creatures assign combat damage as though they weren't blocked?"));
    }
}
