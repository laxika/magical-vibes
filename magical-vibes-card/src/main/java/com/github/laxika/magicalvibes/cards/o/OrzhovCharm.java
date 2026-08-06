package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetAndAttachedMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "185")
public class OrzhovCharm extends Card {

    public OrzhovCharm() {
        // Choose one —
        // • Return target creature you control and all Auras you control attached to it to their owner's hand.
        // • Destroy target creature and you lose life equal to its toughness.
        // • Return target creature card with mana value 1 or less from your graveyard to the battlefield.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature you control and all Auras you control attached to it to their owner's hand",
                        new ReturnTargetAndAttachedMatchingToHandEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.AURA),
                                        new PermanentControlledBySourceControllerPredicate()))),
                        TargetFilters.creatureYouControl()),
                // Life loss is listed first so TargetToughness reads the creature while it is still on
                // the battlefield; the amount is its toughness as it last existed there.
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature and you lose life equal to its toughness",
                        List.of(new LoseLifeEffect(new TargetToughness(), LoseLifeRecipient.CONTROLLER),
                                new DestroyTargetPermanentEffect()),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card with mana value 1 or less from your graveyard to the battlefield",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardMaxManaValuePredicate(1))))
                                .targetGraveyard(true)
                                .build())
        )));
    }
}
