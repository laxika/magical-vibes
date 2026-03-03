package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "122")
public class PhyrexianRevoker extends Card {

    public PhyrexianRevoker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect(List.of(CardType.LAND)));
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(true));
    }
}
