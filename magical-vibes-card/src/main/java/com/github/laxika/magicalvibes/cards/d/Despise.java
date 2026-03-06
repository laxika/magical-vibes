package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "56")
public class Despise extends Card {

    public Despise() {
        addEffect(EffectSlot.SPELL, new ChooseCardFromTargetHandToDiscardEffect(1, List.of(), List.of(CardType.CREATURE, CardType.PLANESWALKER)));
    }
}
