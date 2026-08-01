package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;

@CardRegistration(set = "RTR", collectorNumber = "19")
public class RootbornDefenses extends Card {

    public RootbornDefenses() {
        // Populate. Creatures you control gain indestructible until end of turn.
        // Populate resolves first, so the token copy it creates is also a creature you
        // control when the grant applies.
        addEffect(EffectSlot.SPELL, new PopulateEffect());
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES));
    }
}
