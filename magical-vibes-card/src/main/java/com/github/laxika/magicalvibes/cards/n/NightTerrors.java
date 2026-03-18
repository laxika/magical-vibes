package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToExileEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "111")
public class NightTerrors extends Card {

    public NightTerrors() {
        addEffect(EffectSlot.SPELL, new ChooseCardFromTargetHandToExileEffect(1, List.of(CardType.LAND)));
    }
}
