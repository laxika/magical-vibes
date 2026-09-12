package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "18")
public class ImperialRecoveryUnit extends Card {

    public ImperialRecoveryUnit() {
        var creatureOrVehicle = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));
        var eligibleCard = new CardAllOfPredicate(List.of(
                creatureOrVehicle,
                new CardMaxManaValuePredicate(2)));

        addEffect(EffectSlot.ON_ATTACK, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(eligibleCard)
                .targetGraveyard(true)
                .build());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2"));
    }
}
