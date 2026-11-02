package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellFromGraveyardOrActivatedGraveyardAbilityThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCD", collectorNumber = "56")
public class LaboratoryDrudge extends Card {

    public LaboratoryDrudge() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new ControllerCastSpellFromGraveyardOrActivatedGraveyardAbilityThisTurn(),
                new DrawCardEffect()));
    }
}
