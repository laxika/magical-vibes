package com.github.laxika.magicalvibes.cards.p;

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

@CardRegistration(set = "PLC", collectorNumber = "44")
public class Pongify extends Card {

    public Pongify() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentEffect(true, new CreateTokenEffect(
                        "Ape", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.APE), Set.of(), Set.of())));
    }
}
