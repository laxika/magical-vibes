package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilitiesOfCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "242")
@CardRegistration(set = "WOE", collectorNumber = "366")
public class AgathasSoulCauldron extends Card {

    public AgathasSoulCauldron() {
        addStaticEffect(SpendManaAsAnyColorForActivatedAbilitiesEffect.controlledCreatures());
        addStaticEffect(new GrantActivatedAbilitiesOfCardsExiledWithSourceEffect(
                CardType.CREATURE, Subjects.creaturesYouControl(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, true),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.CREATURE)),
                                new QueueReflexiveAbilityEffect(PutCountersEffect.withTargetRestriction(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentControlledBySourceControllerPredicate())))))),
                "{T}: Exile target card from a graveyard. When a creature card is exiled this way, put a +1/+1 counter on target creature you control.",
                new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS)));
    }
}
