package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "22")
public class SunstrikeLegionnaire extends Card {

    public SunstrikeLegionnaire() {
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.SELF));

        var targetCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentMaxManaValuePredicate(3)
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Tap target creature with mana value 3 or less.",
                new PermanentPredicateTargetFilter(
                        targetCreature,
                        "Target must be a creature with mana value 3 or less")
        ));
    }
}
