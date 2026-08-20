package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "95")
public class RimescaleDragon extends Card {

    public RimescaleDragon() {
        addEffect(EffectSlot.STATIC, new MatchingPermanentsDoesntUntapEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.ICE)
                ))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{S}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.ICE)
                ),
                "{2}{S}: Tap target creature and put an ice counter on it.",
                TargetFilters.creature()
        ));
    }
}
