package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceEquipCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "137")
@CardRegistration(set = "FIN", collectorNumber = "386")
@CardRegistration(set = "FIN", collectorNumber = "459")
@CardRegistration(set = "FIN", collectorNumber = "531")
public class FirionWildRoseWarrior extends Card {

    public FirionWildRoseWarrior() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HASTE, GrantScope.ALL_OWN_CREATURES, new PermanentIsEquippedPredicate()));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.EQUIPMENT,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                        true, List.of(StaticAbility.of(List.of(ReduceEquipCostEffect.ofThisEquipmentOnly(2)))))
                        .ofTriggeringPermanent());
    }
}
