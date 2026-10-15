package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "235")
public class SatsukiTheLivingLore extends Card {

    public SatsukiTheLivingLore() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCounterOnEachControlledPermanentEffect(
                        CounterType.LORE, 1, new PermanentHasSubtypePredicate(CardSubtype.SAGA))),
                "Put a lore counter on each Saga you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));

        PermanentAllOfPredicate controlledPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.SAGA),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentIsCreaturePredicate()))))));
        PermanentPredicateTargetFilter controlledSagaOrEnchantmentCreature = new PermanentPredicateTargetFilter(
                controlledPermanent, "Target must be a Saga or enchantment creature you control");

        CardSubtypePredicate sagaCard = new CardSubtypePredicate(CardSubtype.SAGA);
        GraveyardSearchScope ownGraveyard = GraveyardSearchScope.CONTROLLERS_GRAVEYARD;

        addEffect(EffectSlot.ON_DEATH, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Saga or enchantment creature you control to its owner's hand",
                        ReturnToHandEffect.target(), controlledSagaOrEnchantmentCreature),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target Saga card from your graveyard to your hand",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(sagaCard)
                                .source(ownGraveyard)
                                .targetGraveyard(true)
                                .build(),
                        new GraveyardCardPredicateTargetFilter(sagaCard, ownGraveyard)
                )), true, 0, 1));
    }
}
