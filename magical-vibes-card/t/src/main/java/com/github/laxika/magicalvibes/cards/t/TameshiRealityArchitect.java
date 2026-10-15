package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "82")
public class TameshiRealityArchitect extends Card {

    public TameshiRealityArchitect() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_RETURNED_TO_HAND,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                        new OncePerTurnTriggerEffect(new DrawCardEffect())));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{W}",
                List.of(
                        new ReturnMultiplePermanentsToHandCost(1, new PermanentIsLandPredicate()),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                        new CardTypePredicate(CardType.ENCHANTMENT))))
                                .targetGraveyard(true)
                                .requiresManaValueAtMostX(true)
                                .build()),
                "{X}{W}, Return a land you control to its owner's hand: Return target artifact or enchantment card "
                        + "with mana value X or less from your graveyard to the battlefield. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
