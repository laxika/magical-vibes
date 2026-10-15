package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect;

@CardRegistration(set = "RAV", collectorNumber = "192")
public class BloodbondMarch extends Card {

    public BloodbondMarch() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect());
    }
}
