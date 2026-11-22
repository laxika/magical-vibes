package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "278")
@CardRegistration(set = "MSC", collectorNumber = "340")
@CardRegistration(set = "SLD", collectorNumber = "1897")
@CardRegistration(set = "HOC", collectorNumber = "18")
@CardRegistration(set = "HOC", collectorNumber = "58")
@CardRegistration(set = "LTC", collectorNumber = "23")
@CardRegistration(set = "LTC", collectorNumber = "106")
@CardRegistration(set = "LTC", collectorNumber = "425")
public class RaiseThePalisade extends Card {

    public RaiseThePalisade() {
        addSpellEffect(new ChooseCreatureTypeAtResolutionEffect());
        addSpellEffect(ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasTypeChosenThisResolutionPredicate())))));
    }
}
