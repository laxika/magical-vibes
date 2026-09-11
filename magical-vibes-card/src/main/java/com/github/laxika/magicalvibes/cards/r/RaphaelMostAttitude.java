package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "TMT", collectorNumber = "101")
@CardRegistration(set = "TMT", collectorNumber = "199")
public class RaphaelMostAttitude extends Card {

    public RaphaelMostAttitude() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new MayEffect(
                new ExileTopCardsToSourceEffect(1, false),
                "Exile the top card of your library?"));

        addEffect(EffectSlot.ON_ATTACK,
                new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(null, false));
    }
}
