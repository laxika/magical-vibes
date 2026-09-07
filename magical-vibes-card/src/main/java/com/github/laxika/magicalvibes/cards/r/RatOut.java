package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "103")
public class RatOut extends Card {

    public RatOut() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1,
                "Rat",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.RAT),
                Set.of(),
                Set.of(),
                Map.of(EffectSlot.STATIC, new CantBlockEffect())
        ));
    }
}
