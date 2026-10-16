package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "46")
public class HowlersHeavy extends Card {

    public HowlersHeavy() {
        PermanentPredicate creatureOrVehicle = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        PermanentPredicate opponentCreatureOrVehicle = new PermanentAllOfPredicate(List.of(
                creatureOrVehicle,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        // When you cycle this card, target creature or Vehicle an opponent controls gets -3/-0
        // until end of turn. The reflexive cycle trigger resolves before the cycling draw.
        target(new PermanentPredicateTargetFilter(
                opponentCreatureOrVehicle,
                "Target must be a creature or Vehicle an opponent controls"))
                .addEffect(EffectSlot.ON_SELF_CYCLED, new BoostTargetPermanentEffect(-3, 0, opponentCreatureOrVehicle));
        addCycling("{1}{U}");
    }
}
