package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "36")
@CardRegistration(set = "MSC", collectorNumber = "336")
public class LockjawSlobberingTeleporter extends Card {

    public LockjawSlobberingTeleporter() {
        PermanentPredicate otherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        SpellTarget otherCreatureTarget = target(new PermanentPredicateTargetFilter(
                otherCreatureYouControl, "Target must be another creature you control"), 0, 1);

        SequenceEffect unblockableCreatures = SequenceEffect.of(
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect()),
                new MakeCreatureUnblockableEffect());
        registerEffectTargetIndex(unblockableCreatures, otherCreatureTarget.getIndex());

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new ControllerCastSpellThisTurn(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PutCountersOnSelfThenReflexiveEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, unblockableCreatures)));
    }
}
