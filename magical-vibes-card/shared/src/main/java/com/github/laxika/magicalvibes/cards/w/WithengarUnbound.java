package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

/**
 * Withengar Unbound — back face of Elbrus, the Binding Blade.
 * 13/13 Legendary Demon with flying, intimidate, and trample (keywords loaded from Scryfall).
 * (Transforms from Elbrus, the Binding Blade.)
 *
 * <p>"Whenever a player loses the game, put thirteen +1/+1 counters on Withengar Unbound."
 * This is a multiplayer-only ability: this engine is strictly 2-player and the game ends the
 * instant a player loses, so the trigger goes onto the stack but never resolves before the
 * game finishes. See {@code GameOutcomeService.firePlayerLosesGameTriggers}.
 */
public class WithengarUnbound extends Card {

    public WithengarUnbound() {
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 13));
    }
}
