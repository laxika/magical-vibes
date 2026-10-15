package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "72")
public class WeakstonesSubjugation extends Card {

    public WeakstonesSubjugation() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be an artifact or creature"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayPayManaEffect("{3}",
                                new TapPermanentsEffect(TapUntapScope.ENCHANTED),
                                "Pay {3} to tap enchanted permanent?"))
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
