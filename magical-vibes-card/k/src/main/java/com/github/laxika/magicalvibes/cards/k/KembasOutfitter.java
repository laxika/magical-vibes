package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandToPerpetuallyGrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantActivatedAbilityToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "2")
public class KembasOutfitter extends Card {

    public KembasOutfitter() {
        EquipActivatedAbility equipAbility = new EquipActivatedAbility("{1}");
        PermanentPredicate equipmentYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                new PermanentControlledBySourceControllerPredicate()));
        PermanentPredicateTargetFilter equipmentTarget = new PermanentPredicateTargetFilter(
                equipmentYouControl, "Target must be an Equipment you control");

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose an Equipment card in your hand. It perpetually gains equip {1}.",
                        new ChooseCardFromHandToPerpetuallyGrantActivatedAbilityEffect(
                                equipAbility, new CardSubtypePredicate(CardSubtype.EQUIPMENT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Target Equipment you control perpetually gains equip {1}.",
                        new PerpetuallyGrantActivatedAbilityToTargetPermanentEffect(
                                equipAbility, equipmentYouControl),
                        equipmentTarget)
        )));
    }
}
