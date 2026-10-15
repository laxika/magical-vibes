package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControllerCycledCardNamedAtLeastThisGame;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "141")
public class YidaroWanderingMonster extends Card {

    private static final String NAME = "Yidaro, Wandering Monster";

    public YidaroWanderingMonster() {
        ControllerCycledCardNamedAtLeastThisGame threshold =
                new ControllerCycledCardNamedAtLeastThisGame(4, NAME);
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(
                ConditionalEffect.unless(threshold, ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                ConditionalEffect.unless(new NotCondition(threshold),
                        new ShuffleSelfFromGraveyardIntoLibraryEffect()),
                new DrawCardEffect(1)
        ), "Cycling {1}{R} ({1}{R}, Discard this card: Draw a card.)"));
    }
}
