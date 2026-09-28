package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "12")
@CardRegistration(set = "PIP", collectorNumber = "540")
public class BrotherhoodOutcast extends Card {

    public BrotherhoodOutcast() {
        CardPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
        CardPredicate eligibleCard = new CardAllOfPredicate(List.of(
                auraOrEquipment,
                new CardMaxManaValuePredicate(3)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Aura or Equipment card with mana value 3 or less from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(eligibleCard)
                                .targetGraveyard(true)
                                .chooseAuraAttachment(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(
                                eligibleCard, GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a shield counter on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.SHIELD),
                        TargetFilters.creature())
        )));
    }
}
