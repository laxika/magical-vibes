package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "MH1", collectorNumber = "11")
@CardRegistration(set = "SLZ", collectorNumber = "4")
@CardRegistration(set = "SLZ", collectorNumber = "125")
@CardRegistration(set = "SLZ", collectorNumber = "246")
public class GenerousGift extends Card {

    public GenerousGift() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentEffect(false, new CreateTokenEffect(
                        "Elephant", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.ELEPHANT), Set.of(), Set.of())));
    }
}
