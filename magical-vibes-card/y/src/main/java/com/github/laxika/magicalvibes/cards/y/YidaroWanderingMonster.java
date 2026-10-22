package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControllerCycledCardNamedAtLeastThisGame;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "IKO", collectorNumber = "141")
public class YidaroWanderingMonster extends Card {

    private static final String NAME = "Yidaro, Wandering Monster";

    public YidaroWanderingMonster() {
        ControllerCycledCardNamedAtLeastThisGame threshold =
                new ControllerCycledCardNamedAtLeastThisGame(4, NAME);
        addEffect(EffectSlot.ON_SELF_CYCLED, ConditionalEffect.unless(threshold, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsSelfPredicate())
                .returnAll(true)
                .build()));
        addEffect(EffectSlot.ON_SELF_CYCLED, ConditionalEffect.unless(new NotCondition(threshold),
                new ShuffleSelfFromGraveyardIntoLibraryEffect()));
        addCycling("{1}{R}");
    }
}
