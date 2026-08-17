package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "161")
public class ConclaveCavalier extends Card {

    public ConclaveCavalier() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                2, "Elf Knight", 2, 2, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.ELF, CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE), Set.of()
        ));
    }
}
