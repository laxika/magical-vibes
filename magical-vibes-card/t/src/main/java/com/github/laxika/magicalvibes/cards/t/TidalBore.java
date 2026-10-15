package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "109")
public class TidalBore extends Card {

    public TidalBore() {
        addCastingOption(new AlternateHandCast(List.of(
                new ReturnPermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.ISLAND))
        )));
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target creature?"));
    }
}
