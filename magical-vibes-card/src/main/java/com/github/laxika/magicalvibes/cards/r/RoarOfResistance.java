package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "ONC", collectorNumber = "15")
@CardRegistration(set = "ONC", collectorNumber = "53")
public class RoarOfResistance extends Card {

    public RoarOfResistance() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE,
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTokenPredicate()))));

        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS, new MayPayManaEffect(
                "{1}{R}",
                new BoostAllCreaturesEffect(
                        2, 0, new PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate()),
                "Pay {1}{R} to boost attacking creatures?"));
    }
}
