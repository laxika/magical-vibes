package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "TLE", collectorNumber = "78")
@CardRegistration(set = "TLE", collectorNumber = "172")
public class JetRebelLeader extends Card {

    private static final CardPredicate ELIGIBLE_CREATURE = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardMaxManaValuePredicate(3)));

    private static final LibrarySelectionFollowUp ATTACK_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            return new MakeChosenPermanentAttackingEffect(selectedPermanentIds.getFirst());
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

    public JetRebelLeader() {
        addEffect(EffectSlot.ON_ATTACK, new LookAtTopCardsEffect(
                new Fixed(5),
                new Fixed(1),
                ELIGIBLE_CREATURE,
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
                ATTACK_FOLLOW_UP));
    }
}
