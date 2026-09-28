package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "86")
public class TheFlux extends Card {

    private static final PermanentPredicate OPPONENT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public TheFlux() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToTargetCreatureEffect(4, OPPONENT_CREATURE));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(OPPONENT_CREATURE,
                        "Must target a creature an opponent controls")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.SAGA_CHAPTER_V, new ExileTopCardMayPlayThisTurnEffect(false));
        addEffect(EffectSlot.SAGA_CHAPTER_VI, new AwardManaEffect(ManaColor.RED, 6));
    }
}
