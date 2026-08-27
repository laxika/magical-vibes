package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

/**
 * Back face of Zenos yae Galvus. The two-player engine has one opponent, so the chosen opponent
 * is represented by the global player-loss trigger.
 */
public class ShinryuTranscendentRival extends Card {

    public ShinryuTranscendentRival() {
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME, new WinGameEffect());
    }
}
