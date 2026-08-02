package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "44")
public class RapidHybridization extends Card {

    public RapidHybridization() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentEffect(true, new CreateTokenEffect(
                        "Frog Lizard", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.FROG, CardSubtype.LIZARD), Set.of(), Set.of())));
    }
}
