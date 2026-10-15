package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "22")
public class OneLastJob extends Card {

    public OneLastJob() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        CardAnyOfPredicate mountOrVehicle = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.MOUNT),
                new CardSubtypePredicate(CardSubtype.VEHICLE)));
        CardAnyOfPredicate auraOrEquipment = new CardAnyOfPredicate(List.of(
                new CardIsAuraPredicate(),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)));

        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{2}", "{1}", "{1}")));
        setAllowSharedTargets(true);
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(creature)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(creature,
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Mount or Vehicle card from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(mountOrVehicle)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(mountOrVehicle,
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Aura or Equipment card from your graveyard to the battlefield attached to a creature you control",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(auraOrEquipment)
                                .targetGraveyard(true)
                                .attachmentTarget(new PermanentIsCreaturePredicate())
                                .build(),
                        new GraveyardCardPredicateTargetFilter(auraOrEquipment,
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
        )));
    }
}
