package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellFromGraveyardOrActivatedGraveyardAbilityThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SCD", collectorNumber = "56")
public class LaboratoryDrudge extends Card {

    public LaboratoryDrudge() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new ControllerCastSpellFromGraveyardOrActivatedGraveyardAbilityThisTurn(),
                new DrawCardEffect()));
    }
}
