package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "103")
@CardRegistration(set = "DDL", collectorNumber = "69")
@CardRegistration(set = "PC2", collectorNumber = "57")
@CardRegistration(set = "PCA", collectorNumber = "57")
@CardRegistration(set = "SLD", collectorNumber = "180")
@CardRegistration(set = "SLD", collectorNumber = "181")
@CardRegistration(set = "SLD", collectorNumber = "323")
@CardRegistration(set = "TSR", collectorNumber = "357")
@CardRegistration(set = "MAR", collectorNumber = "33")
@CardRegistration(set = "MAR", collectorNumber = "75")
@CardRegistration(set = "OMB", collectorNumber = "33")
@CardRegistration(set = "SOC", collectorNumber = "263")
@CardRegistration(set = "MSC", collectorNumber = "169")
public class BeastWithin extends Card {

    public BeastWithin() {
        addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(false,
                new CreateTokenEffect("Beast", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.BEAST), Set.of(), Set.of())
        ));
    }
}
