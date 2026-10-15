package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "88")
public class ArchenemysCharm extends Card {

    public ArchenemysCharm() {
        PermanentPredicate creatureOrPlaneswalker = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsPlaneswalkerPredicate()));
        var creatureOrPlaneswalkerTarget = new PermanentPredicateTargetFilter(
                creatureOrPlaneswalker,
                "Target must be a creature or planeswalker.");

        PermanentPredicate creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        var creatureYouControlTarget = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control.");

        CardPredicate creatureOrPlaneswalkerCard = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.PLANESWALKER)));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature or planeswalker",
                        new ExileTargetPermanentEffect(creatureOrPlaneswalker),
                        creatureOrPlaneswalkerTarget),
                new ChooseOneEffect.ChooseOneOption(
                        "Return one or two target creature and/or planeswalker cards from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                creatureOrPlaneswalkerCard, 2, null, false, false, 1,
                                false, Set.of(), false)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on target creature you control. It gains lifelink until end of turn",
                        List.of(
                                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                        CounterType.PLUS_ONE_PLUS_ONE, 2, creatureYouControl),
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET)),
                        creatureYouControlTarget)
        )));
    }
}
