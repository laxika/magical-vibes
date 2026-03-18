package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SpellsWithChosenNameCantBeCastEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "25")
public class Nevermore extends Card {

    public Nevermore() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect(List.of(CardType.LAND)));
        addEffect(EffectSlot.STATIC, new SpellsWithChosenNameCantBeCastEffect());
    }
}
