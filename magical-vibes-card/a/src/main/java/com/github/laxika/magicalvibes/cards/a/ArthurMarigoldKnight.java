package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelectedPermanentToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "BLC", collectorNumber = "41")
public class ArthurMarigoldKnight extends Card {

    private static final LibrarySelectionFollowUp CREATURE_ATTACK_AND_RETURN_FOLLOW_UP =
            new LibrarySelectionFollowUp() {
                @Override
                public CardEffect createEffect(List<UUID> selectedPermanentIds) {
                    UUID selectedPermanentId = selectedPermanentIds.getFirst();
                    return SequenceEffect.of(
                            new MakeChosenPermanentAttackingEffect(selectedPermanentId),
                            ReturnSelectedPermanentToHandAtEndOfCombatEffect
                                    .forSelectedPermanent(selectedPermanentIds));
                }

                @Override
                public String prompt() {
                    return "Choose the player or planeswalker for the creature to attack.";
                }

                @Override
                public boolean optional() {
                    return false;
                }
            };

    public ArthurMarigoldKnight() {
        CardEffect ability = new LookAtTopCardsEffect(
                new Fixed(6),
                new Fixed(1),
                new CardTypePredicate(CardType.CREATURE),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM,
                false,
                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                true,
                false,
                null,
                null,
                false,
                0,
                false,
                false,
                false,
                false,
                0,
                CREATURE_ATTACK_AND_RETURN_FOLLOW_UP);
        addEffect(EffectSlot.ON_ATTACK,
                new ConditionalEffect(new MinimumAttackers(2), ability));
    }
}
