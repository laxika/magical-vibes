package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;

@CardRegistration(set = "USG", collectorNumber = "96")
@CardRegistration(set = "SPG", collectorNumber = "21")
@CardRegistration(set = "MAR", collectorNumber = "60")
public class ShowAndTell extends Card {

    public ShowAndTell() {
        addEffect(EffectSlot.SPELL, EachPlayerMayPutCardFromHandToBattlefieldEffect.showAndTell());
    }
}
