package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "88")
@CardRegistration(set = "MAR", collectorNumber = "65")
public class ExtinctionEvent extends Card {

    public ExtinctionEvent() {
        addSpellEffect(new ChooseManaValueParityAtResolutionEffect());
        addSpellEffect(new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasChosenParityPredicate(PermanentHasChosenParityPredicate.Quality.MANA_VALUE)))));
    }
}
