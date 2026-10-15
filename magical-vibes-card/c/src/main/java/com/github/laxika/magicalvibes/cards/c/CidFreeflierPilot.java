package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "13")
@CardRegistration(set = "FIC", collectorNumber = "131")
public class CidFreeflierPilot extends Card {

    public CidFreeflierPilot() {
        CardAnyOfPredicate equipmentOrVehicle = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                equipmentOrVehicle, 1, CostModificationScope.SELF));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(equipmentOrVehicle)
                        .targetGraveyard(true)
                        .build()),
                "{2}, {T}: Return target Equipment or Vehicle card from your graveyard to your hand."
        ));
    }
}
