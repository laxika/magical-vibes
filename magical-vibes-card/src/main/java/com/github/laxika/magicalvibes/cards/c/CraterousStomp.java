package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockOnlyIfAttackerMatchesPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToOtherCreaturesControlledByTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "11")
public class CraterousStomp extends Card {

    public CraterousStomp() {
        var creatureAnOpponentControls = TargetFilters.creatureAnOpponentControls().predicate();
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3, creatureAnOpponentControls))
                .addEffect(EffectSlot.SPELL, new GrantStaticEffectToOtherCreaturesControlledByTargetEffect(
                        new GrantSubtypeEffect(CardSubtype.COWARD, GrantScope.TARGET)))
                .addEffect(EffectSlot.SPELL, new GrantStaticEffectToOtherCreaturesControlledByTargetEffect(
                        new CanBlockOnlyIfAttackerMatchesPredicateEffect(
                                new PermanentNotPredicate(new PermanentHasAnySubtypePredicate(
                                        Set.of(CardSubtype.GIANT, CardSubtype.WARRIOR))),
                                "creatures that aren't Giants or Warriors")));
    }
}
