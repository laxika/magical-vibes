package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithSourceDrawAndTransformTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "1")
@CardRegistration(set = "BOT", collectorNumber = "16")
public class ProwlStoicStrategist extends Card {

    public ProwlStoicStrategist() {
        setBackFaceCard(new ProwlPursuitVehicle());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{W}"));

        PermanentPredicate tappedCreatureOrVehicle = new PermanentAllOfPredicate(List.of(
                new PermanentIsTappedPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        target(new PermanentPredicateTargetFilter(
                tappedCreatureOrVehicle,
                "Target must be another tapped creature or Vehicle"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect());

        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new PlayedCardExiledWithSourceDrawAndTransformTriggerEffect());
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new PlayedCardExiledWithSourceDrawAndTransformTriggerEffect());
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND,
                new PlayedCardExiledWithSourceDrawAndTransformTriggerEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ProwlPursuitVehicle";
    }
}
