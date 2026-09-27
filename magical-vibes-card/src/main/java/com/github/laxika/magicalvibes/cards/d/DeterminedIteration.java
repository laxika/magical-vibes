package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

@CardRegistration(set = "SOC", collectorNumber = "122")
public class DeterminedIteration extends Card {

    public DeterminedIteration() {
        // Populate, then the token created this way gains haste.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new PopulateEffect());
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TOKENS_CREATED_THIS_RESOLUTION));

        // Sacrifice the populated token at the beginning of the next end step.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
