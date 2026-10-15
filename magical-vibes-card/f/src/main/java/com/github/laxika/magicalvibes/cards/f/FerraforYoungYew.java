package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECC", collectorNumber = "16")
@CardRegistration(set = "ECC", collectorNumber = "36")
public class FerraforYoungYew extends Card {

    public FerraforYoungYew() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new PermanentCounterSum(null, new PermanentIsCreaturePredicate(), CountScope.TARGET_PLAYER),
                "Saproling", 1, 1, CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DoubleCountersOnTargetPermanentEffect()),
                "{T}: Double the number of each kind of counter on target creature.",
                TargetFilters.creature()
        ));
    }
}
