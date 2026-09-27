package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "201")
public class GracefulRestoration extends Card {

    public GracefulRestoration() {
        var ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
        var creature = new CardTypePredicate(CardType.CREATURE);
        var smallCreature = new CardAllOfPredicate(List.of(
                creature,
                new CardPowerAtMostPredicate(2)));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield with an additional +1/+1 counter on it",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(creature)
                                .source(ownGraveyard)
                                .targetGraveyard(true)
                                .plusOneCounterCount(1)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(creature, ownGraveyard)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to two target creature cards with power 2 or less from your graveyard to the battlefield",
                        List.of(ReturnTargetCardsFromGraveyardToBattlefieldEffect.withTargetBounds(
                                smallCreature, 2, 0)))
        )));
    }
}
