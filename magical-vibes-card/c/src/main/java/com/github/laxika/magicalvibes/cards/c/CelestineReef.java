package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;

@CardRegistration(set = "OPCA", collectorNumber = "17")
public class CelestineReef extends Card {

    public CelestineReef() {
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackUnlessPredicateEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentHasKeywordPredicate(Keyword.ISLANDWALK)
                ))));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantStaticEffectToPlayerUntilPlaneswalkEffect(new CantLoseGameEffect()));
    }
}
