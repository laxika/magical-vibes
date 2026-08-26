package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MID", collectorNumber = "8")
public class CandlegroveWitch extends Card {

    public CandlegroveWitch() {
        // Coven — At the beginning of combat on your turn, if you control three or more creatures
        // with different powers, this creature gains flying until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new Coven(), new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
    }
}
