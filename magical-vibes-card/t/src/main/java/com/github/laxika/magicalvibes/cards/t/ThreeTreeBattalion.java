package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "YBLB", collectorNumber = "4")
public class ThreeTreeBattalion extends Card {

    private static final LibrarySelectionFollowUp DUPLICATE_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            return new ConjureDuplicateOfSelectedPermanentOntoBattlefieldEffect(
                    selectedPermanentIds.getFirst(), 1, 1);
        }

        @Override
        public String prompt() {
            return "Create a duplicate of that card.";
        }

        @Override
        public boolean optional() {
            return false;
        }
    };

    public ThreeTreeBattalion() {
        addSpellEffect(LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                6,
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(3))),
                DUPLICATE_FOLLOW_UP));
    }
}
