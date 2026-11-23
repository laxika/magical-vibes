package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "156")
@CardRegistration(set = "MSC", collectorNumber = "351")
@CardRegistration(set = "CMM", collectorNumber = "169")
@CardRegistration(set = "CMM", collectorNumber = "515")
@CardRegistration(set = "CMM", collectorNumber = "640")
@CardRegistration(set = "WOC", collectorNumber = "113")
@CardRegistration(set = "C17", collectorNumber = "18")
public class KindredDominance extends Card {

    public KindredDominance() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasTypeChosenThisResolutionPredicate())))));
    }
}
