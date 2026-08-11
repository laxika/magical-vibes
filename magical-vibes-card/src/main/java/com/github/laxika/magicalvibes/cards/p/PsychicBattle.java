package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PsychicBattleEffect;

@CardRegistration(set = "INV", collectorNumber = "68")
public class PsychicBattle extends Card {

    public PsychicBattle() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CHOOSES_TARGETS, new PsychicBattleEffect());
    }
}
