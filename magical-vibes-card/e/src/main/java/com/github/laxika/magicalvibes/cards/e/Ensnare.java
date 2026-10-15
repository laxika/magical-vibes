package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "32")
public class Ensnare extends Card {

    public Ensnare() {
        addCastingOption(new AlternateHandCast(List.of(
                new ReturnPermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND))
        )));
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.ALL_CREATURES));
    }
}
