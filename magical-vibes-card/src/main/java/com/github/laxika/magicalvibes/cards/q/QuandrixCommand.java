package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "217")
public class QuandrixCommand extends Card {

    public QuandrixCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature or planeswalker to its owner's hand",
                        ReturnToHandEffect.target(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()
                                )),
                                "Target must be a creature or planeswalker."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target artifact or enchantment spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryAllOfPredicate(List.of(
                                        new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT, CardType.ENCHANTMENT)),
                                        new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(
                                                StackEntryType.ACTIVATED_ABILITY,
                                                StackEntryType.TRIGGERED_ABILITY
                                        )))
                                )),
                                "Target must be an artifact or enchantment spell."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature."
                        )),
                new ChooseOneEffect.ChooseOneOption(
                        "Target player shuffles up to three target cards from their graveyard into their library",
                        new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 3),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player."
                        ))
        ), 2));
    }
}
