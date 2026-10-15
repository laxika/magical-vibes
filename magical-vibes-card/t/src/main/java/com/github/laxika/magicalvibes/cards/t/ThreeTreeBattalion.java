package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSelectedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CardRegistration(set = "YBLB", collectorNumber = "4")
public class ThreeTreeBattalion extends Card {

    private static final LibrarySelectionFollowUp DUPLICATE_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            return new CreateTokenCopyOfSelectedPermanentEffect(
                    selectedPermanentIds.getFirst(),
                    new CreateTokenCopyOfTargetPermanentEffect(
                            List.of(), Set.of(), 1, 1, Map.of()));
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
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                6,
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(3))),
                DUPLICATE_FOLLOW_UP));
    }
}
