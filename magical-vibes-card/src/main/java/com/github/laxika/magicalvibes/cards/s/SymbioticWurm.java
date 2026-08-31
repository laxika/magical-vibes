package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONS", collectorNumber = "289")
public class SymbioticWurm extends Card {

    public SymbioticWurm() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                7, "Insect", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.INSECT), Set.of(), Set.of()));
    }
}
