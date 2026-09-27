package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TDC", collectorNumber = "111")
public class CommandersInsignia extends Card {

    public CommandersInsignia() {
        var commanderCasts = new CommanderCastsFromCommandZoneThisGame();
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                commanderCasts, commanderCasts, GrantScope.OWN_CREATURES));
    }
}
