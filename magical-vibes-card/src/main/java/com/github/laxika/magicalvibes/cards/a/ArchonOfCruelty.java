package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "75")
public class ArchonOfCruelty extends Card {

    private static final PermanentPredicate CREATURE_OR_PLANESWALKER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentIsPlaneswalkerPredicate()));

    private static final SequenceEffect CRUELTY = SequenceEffect.of(
            new SacrificePermanentsEffect(1, CREATURE_OR_PLANESWALKER, SacrificeRecipient.TARGET_PLAYER),
            new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
            new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER),
            new DrawCardEffect(1),
            new GainLifeEffect(3));

    public ArchonOfCruelty() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CRUELTY)
                .addEffect(EffectSlot.ON_ATTACK, CRUELTY);
    }
}
