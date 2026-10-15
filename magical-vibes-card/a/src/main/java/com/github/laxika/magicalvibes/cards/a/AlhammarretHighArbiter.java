package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "43")
public class AlhammarretHighArbiter extends Card {

    public AlhammarretHighArbiter() {
        // As it enters, each opponent reveals their hand and the controller names a nonland card
        // among the revealed ones.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect(
                List.of(CardType.LAND), ChooseCardNameOnEnterEffect.HandAccess.REVEAL_OPPONENT_HAND));
        // Your opponents can't cast spells with the chosen name (opponents-only).
        addEffect(EffectSlot.STATIC, new SpellsWithChosenNameCantBeCastEffect(true));
    }
}
