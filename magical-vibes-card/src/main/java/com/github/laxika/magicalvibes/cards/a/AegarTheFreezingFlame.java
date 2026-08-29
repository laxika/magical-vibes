package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GiantWizardOrSpellDealtDamageToTargetThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KHM", collectorNumber = "200")
public class AegarTheFreezingFlame extends Card {

    public AegarTheFreezingFlame() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE,
                new ConditionalEffect(new GiantWizardOrSpellDealtDamageToTargetThisTurn(), new DrawCardEffect()));
    }
}
