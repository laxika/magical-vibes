package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "118")
public class RimehornAurochs extends Card {

    public RimehornAurochs() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new PermanentCount(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.AUROCHS))),
                        CountScope.ANY_PLAYER,
                        true),
                new Fixed(0)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{S}",
                List.of(new MustBlockTargetCreatureEffect()),
                "Target creature blocks target creature this turn if able.",
                List.of(TargetFilters.creature(), TargetFilters.creature()),
                2,
                2
        ));
    }
}
