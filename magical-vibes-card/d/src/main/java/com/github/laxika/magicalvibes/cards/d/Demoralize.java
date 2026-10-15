package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "ODY", collectorNumber = "184")
public class Demoralize extends Card {

    public Demoralize() {
        // All creatures gain menace until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_CREATURES));

        // Threshold — If there are seven or more cards in your graveyard, creatures can't block this turn.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GraveyardCardThreshold(7, null),
                new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES)));
    }
}
