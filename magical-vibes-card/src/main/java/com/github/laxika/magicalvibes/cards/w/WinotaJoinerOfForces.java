package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LibrarySelectionFollowUp;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.UUID;

@CardRegistration(set = "IKO", collectorNumber = "216")
public class WinotaJoinerOfForces extends Card {

    private static final CardPredicate HUMAN_CREATURE = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.CREATURE),
            new CardSubtypePredicate(CardSubtype.HUMAN)));

    private static final LibrarySelectionFollowUp HUMAN_ATTACK_FOLLOW_UP = new LibrarySelectionFollowUp() {
        @Override
        public CardEffect createEffect(List<UUID> selectedPermanentIds) {
            UUID selectedPermanentId = selectedPermanentIds.getFirst();
            return SequenceEffect.of(
                    new MakeChosenPermanentAttackingEffect(selectedPermanentId),
                    new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(
                            Keyword.INDESTRUCTIBLE, selectedPermanentId));
        }

        @Override
        public String prompt() {
            return "Choose the player or planeswalker for the Human to attack.";
        }

        @Override
        public boolean optional() {
            return false;
        }
    };

    public WinotaJoinerOfForces() {
        CardEffect ability = new LookAtTopCardsEffect(
                new Fixed(6), new Fixed(1), HUMAN_CREATURE,
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                LibrarySearchDestination.BATTLEFIELD_TAPPED, true,
                false, null, null, false, 0,
                false, false, false, false, 0, HUMAN_ATTACK_FOLLOW_UP);
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN)), ability));
    }
}
