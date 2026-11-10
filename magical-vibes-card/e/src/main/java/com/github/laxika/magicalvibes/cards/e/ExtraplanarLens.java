package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "169")
@CardRegistration(set = "MPS", collectorNumber = "38")
@CardRegistration(set = "CMM", collectorNumber = "381")
@CardRegistration(set = "CMM", collectorNumber = "604")
@CardRegistration(set = "CMM", collectorNumber = "656")
public class ExtraplanarLens extends Card {

    public ExtraplanarLens() {
        target(TargetFilters.landYouControl()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ExileTargetPermanentAndImprintEffect(),
                        "Exile target land you control?"));
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentSharesNameWithImprintedCardPredicate()),
                new AddOneOfEachManaTypeProducedByLandEffect());
    }
}
