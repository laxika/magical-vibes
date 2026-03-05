package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "103")
public class BeastWithin extends Card {

    public BeastWithin() {
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(false,
                new CreateCreatureTokenEffect("Beast", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.BEAST), Set.of(), Set.of())
        ));
    }
}
