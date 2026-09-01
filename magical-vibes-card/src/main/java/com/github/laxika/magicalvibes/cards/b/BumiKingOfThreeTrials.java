package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "169")
public class BumiKingOfThreeTrials extends Card {

    public BumiKingOfThreeTrials() {
        CardsInGraveyard lessonCount = new CardsInGraveyard(
                new CardSubtypePredicate(CardSubtype.LESSON), CountScope.CONTROLLER);
        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
        SpellTarget scryTarget = target(playerTarget);
        SpellTarget earthbendTarget = target(TargetFilters.landYouControl());

        PutCountersOnSourceEffect counters = new PutCountersOnSourceEffect(1, 1, 3);
        ScryEffect scry = new ScryEffect(3, LibraryOwner.TARGET_PLAYER);
        EarthbendTargetLandEffect earthbend = new EarthbendTargetLandEffect(3);
        registerEffectTargetIndex(scry, scryTarget.getIndex());
        registerEffectTargetIndex(earthbend, earthbendTarget.getIndex());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("Put three +1/+1 counters on Bumi.", counters),
                        new ChooseOneEffect.ChooseOneOption("Target player scries 3.", scry, playerTarget),
                        new ChooseOneEffect.ChooseOneOption("Earthbend 3.", earthbend, TargetFilters.landYouControl())
                )), lessonCount));
    }
}
