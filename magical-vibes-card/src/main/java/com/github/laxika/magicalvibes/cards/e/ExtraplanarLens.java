package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "169")
public class ExtraplanarLens extends Card {

    public ExtraplanarLens() {
        target(TargetFilters.landYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExileTargetPermanentAndImprintEffect(),
                        "Exile target land you control?"));
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddOneOfEachManaTypeProducedByLandEffect(false, true));
    }
}
