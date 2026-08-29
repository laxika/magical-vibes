package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "30")
public class AerialFormation extends Card {

    public AerialFormation() {
        // Strive — This spell costs {2}{U} more to cast for each target beyond the first.
        setAdditionalManaCostPerExtraTarget("{2}{U}");

        // Any number of target creatures each get +1/+1 and gain flying until end of turn.
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
