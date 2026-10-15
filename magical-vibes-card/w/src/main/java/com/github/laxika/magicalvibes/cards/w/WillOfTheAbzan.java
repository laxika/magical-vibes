package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerLosesLifeAndSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TDC", collectorNumber = "31")
@CardRegistration(set = "TDC", collectorNumber = "71")
public class WillOfTheAbzan extends Card {

    public WillOfTheAbzan() {
        var opponents = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        var greatestPowerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate()));
        var creatureCard = new CardTypePredicate(CardType.CREATURE);
        var ownGraveyardCreature = new GraveyardCardPredicateTargetFilter(
                creatureCard, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Any number of target opponents each sacrifice a creature with the greatest power among creatures that player controls and lose 3 life",
                        List.of(new EachTargetPlayerLosesLifeAndSacrificesCreatureEffect(
                                3, greatestPowerCreature, true)),
                        opponents, null, 0, 99, false, null),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(creatureCard)
                                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                                .targetGraveyard(true)
                                .build(),
                        ownGraveyardCreature)
        ), new ControlledCommanderAsCast()));
    }
}
