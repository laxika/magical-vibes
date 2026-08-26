package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "110")
public class JoinTheDead extends Card {

    public JoinTheDead() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GraveyardCardThreshold(4, new CardIsPermanentPredicate()),
                new BoostTargetCreatureEffect(-5, -5),
                new BoostTargetCreatureEffect(-10, -10)
        ));
    }
}
