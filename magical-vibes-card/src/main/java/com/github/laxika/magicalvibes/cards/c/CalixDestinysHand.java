package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilTargetEnchantmentLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "211")
public class CalixDestinysHand extends Card {

    public CalixDestinysHand() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4, new CardTypePredicate(CardType.ENCHANTMENT))),
                "+1: Look at the top four cards of your library. You may reveal an enchantment card from among them and put that card into your hand. Put the rest on the bottom of your library in a random order."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new ExileTargetPermanentUntilTargetEnchantmentLeavesEffect()),
                "−3: Exile target creature or enchantment you don't control until target enchantment you control leaves the battlefield.",
                null, -3, null, null,
                List.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsEnchantmentPredicate())),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                                "Target must be a creature or enchantment you don't control"),
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentIsEnchantmentPredicate(),
                                "Target must be an enchantment you control")
                ),
                2, 2
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                        .returnAll(true)
                        .build()),
                "−7: Return all enchantment cards from your graveyard to the battlefield."
        ));
    }
}
