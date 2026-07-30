package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "155")
public class SlaughterCry extends Card {

    public SlaughterCry() {
        // Target creature gets +3/+0 and gains first strike until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new Fixed(3), new Fixed(0)))
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET));
    }
}
