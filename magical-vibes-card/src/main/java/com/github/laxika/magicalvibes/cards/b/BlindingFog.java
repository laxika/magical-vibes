package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAllCreaturesEffect;

@CardRegistration(set = "XLN", collectorNumber = "177")
public class BlindingFog extends Card {

    public BlindingFog() {
        // Prevent all damage that would be dealt to creatures this turn.
        addEffect(EffectSlot.SPELL, new PreventAllDamageToAllCreaturesEffect());
        // Creatures you control gain hexproof until end of turn.
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.OWN_CREATURES));
    }
}
