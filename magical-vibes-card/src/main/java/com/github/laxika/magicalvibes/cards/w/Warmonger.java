package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "225")
public class Warmonger extends Card {

    public Warmonger() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(
                        new DealDamageToEachMatchingPermanentEffect(1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                                EachPermanentScope.ALL_PLAYERS),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_PLAYER)),
                "{2}: This creature deals 1 damage to each creature without flying and each player. Any player may activate this ability.")
                .withActivatableByAnyPlayer());
    }
}
