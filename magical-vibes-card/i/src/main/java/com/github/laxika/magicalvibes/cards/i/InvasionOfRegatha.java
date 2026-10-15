package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "148")
public class InvasionOfRegatha extends Card {

    public InvasionOfRegatha() {
        setBackFaceCard(new DisciplesOfTheInferno());
        setAllowSharedTargets(true);

        target(new AnyTargetPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsBattlePredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be another battle or opponent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(4));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "DisciplesOfTheInferno";
    }
}
