package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "WAR", collectorNumber = "238")
public class GodPharaohsStatue extends Card {

    public GodPharaohsStatue() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTruePredicate(), 2, CostModificationScope.OPPONENT));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
