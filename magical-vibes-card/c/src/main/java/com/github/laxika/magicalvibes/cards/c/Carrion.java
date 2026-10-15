package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "111")
public class Carrion extends Card {

    public Carrion() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Insect", 0, 1, CardColor.BLACK, List.of(CardSubtype.INSECT),
                Set.of(), Set.of()
        ));
    }
}
