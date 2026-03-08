package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OpponentPoisonedConditionalEffect;

@CardRegistration(set = "NPH", collectorNumber = "124")
public class ViridianBetrayers extends Card {

    public ViridianBetrayers() {
        // Viridian Betrayers has infect as long as an opponent is poisoned.
        addEffect(EffectSlot.STATIC,
                new OpponentPoisonedConditionalEffect(
                        new GrantKeywordEffect(Keyword.INFECT, GrantScope.SELF)));
    }
}
