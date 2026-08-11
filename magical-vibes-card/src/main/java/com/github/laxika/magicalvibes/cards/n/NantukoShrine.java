package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect;

@CardRegistration(set = "ODY", collectorNumber = "256")
public class NantukoShrine extends Card {

    public NantukoShrine() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CreateSquirrelTokensForSameNameCardsInGraveyardsOnSpellCastEffect());
    }
}
