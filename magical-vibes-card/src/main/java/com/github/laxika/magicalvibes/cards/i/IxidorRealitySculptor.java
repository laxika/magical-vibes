package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "89")
public class IxidorRealitySculptor extends Card {

    public IxidorRealitySculptor() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES, new PermanentIsFaceDownPredicate()));
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}", List.of(new TurnTargetCreatureFaceUpEffect()),
                "{2}{U}: Turn target face-down creature face up.",
                new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentIsFaceDownPredicate())),
                        "Target must be a face-down creature")));
    }
}
