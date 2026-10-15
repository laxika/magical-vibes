package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "25")
public class ReduceToMemory extends Card {

    public ReduceToMemory() {
        target(TargetFilters.nonlandPermanent()).addEffect(EffectSlot.SPELL,
                new ExileTargetPermanentEffect(new CreateTokenEffect(
                        "Spirit", 3, 2, CardColor.RED,
                        Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT))));
    }
}
