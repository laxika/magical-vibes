package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GiantWizardOrSpellDealtDamageToTargetThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "200")
@CardRegistration(set = "KHM", collectorNumber = "321")
@CardRegistration(set = "MUL", collectorNumber = "31")
@CardRegistration(set = "MUL", collectorNumber = "96")
@CardRegistration(set = "MUL", collectorNumber = "161")
public class AegarTheFreezingFlame extends Card {

    public AegarTheFreezingFlame() {
        addTrigger(Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessDamage(),
                new ConditionalEffect(new GiantWizardOrSpellDealtDamageToTargetThisTurn(), new DrawCardEffect()));
    }
}
