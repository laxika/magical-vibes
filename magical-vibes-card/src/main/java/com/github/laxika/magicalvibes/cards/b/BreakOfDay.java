package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

@CardRegistration(set = "DKA", collectorNumber = "3")
public class BreakOfDay extends Card {

    public BreakOfDay() {
        // Creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));

        // Fateful hour — If you have 5 or less life, those creatures gain indestructible until end of turn.
        addEffect(EffectSlot.SPELL, new ControllerLifeAtOrBelowThresholdConditionalEffect(5,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES)));
    }
}
