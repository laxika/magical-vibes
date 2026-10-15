package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;

@CardRegistration(set = "TMP", collectorNumber = "204")
public class Shocker extends Card {

    public Shocker() {
        // Whenever this creature deals damage to a player, that player discards all the cards in
        // their hand, then draws that many cards. The damaged player rides on the trigger's
        // (non-targeting) target.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new TargetPlayerDiscardsHandThenDrawsThatManyEffect());
    }
}
