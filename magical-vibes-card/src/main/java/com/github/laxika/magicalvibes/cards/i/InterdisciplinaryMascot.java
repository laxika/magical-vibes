package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MOM", collectorNumber = "326")
@CardRegistration(set = "MOM", collectorNumber = "377")
public class InterdisciplinaryMascot extends Card {

    public InterdisciplinaryMascot() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(new Fixed(4), 1));
    }
}
