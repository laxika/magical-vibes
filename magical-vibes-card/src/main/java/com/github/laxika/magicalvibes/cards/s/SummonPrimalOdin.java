package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "121")
public class SummonPrimalOdin extends Card {

    private static final PermanentPredicate OPPONENT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public SummonPrimalOdin() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DestroyTargetPermanentEffect(OPPONENT_CREATURE));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(OPPONENT_CREATURE,
                        "Must target a creature an opponent controls")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, GrantEffectToTargetEffect.toSourcePermanent(
                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerLosesGameEffect(null)));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new DrawCardEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
    }
}
