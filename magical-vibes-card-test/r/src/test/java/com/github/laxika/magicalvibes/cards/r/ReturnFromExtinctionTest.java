package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ExcavatingAnurid;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnFromExtinction.class, MotherBear.class, ExcavatingAnurid.class, UniversalAutomaton.class})
class ReturnFromExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("The single-card mode returns a creature card from the graveyard to hand")
    void returnsOneCreatureCard() {
        Card creature = new MotherBear();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mother Bear");
        harness.assertNotInGraveyard(player1, "Mother Bear");
    }

    @Test
    @DisplayName("The two-card mode returns two creature cards that share a creature type")
    void returnsTwoCreaturesSharingType() {
        Card firstBear = new MotherBear();
        Card secondBear = new MotherBear();
        Card anurid = new ExcavatingAnurid();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, anurid));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(firstBear.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Mother Bear");
        harness.assertInGraveyard(player1, "Excavating Anurid");
    }

    @Test
    @DisplayName("The shared-type mode requires a legal creature pair")
    void requiresASharedTypePair() {
        Card bear = new MotherBear();
        Card anurid = new ExcavatingAnurid();
        harness.setGraveyard(player1, List.of(bear, anurid));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Return from Extinction");
    }

    @Test
    void returnsChangelingWithDifferentPrintedCreatureType() {
        Card bear = new MotherBear();
        Card changeling = new UniversalAutomaton();
        harness.setGraveyard(player1, List.of(bear, changeling));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId(), changeling.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(bear, changeling);
        harness.assertNotInGraveyard(player1, "Mother Bear");
        harness.assertNotInGraveyard(player1, "Universal Automaton");
    }

    @Test
    void rejectsUnrelatedPairEvenWhenBothHaveLegalPartners() {
        Card bear = new MotherBear();
        Card anurid = new ExcavatingAnurid();
        Card changeling = new UniversalAutomaton();
        harness.setGraveyard(player1, List.of(bear, anurid, changeling));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(bear.getId(), anurid.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId(), changeling.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(bear, changeling);
        harness.assertInGraveyard(player1, "Excavating Anurid");
    }

    @Test
    void returnsRemainingTargetWhenOtherTargetLeavesGraveyard() {
        Card firstBear = new MotherBear();
        Card secondBear = new MotherBear();
        harness.setGraveyard(player1, List.of(firstBear, secondBear));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.setGraveyard(player1, List.of(secondBear));
        harness.setExile(player1, List.of(firstBear));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondBear);
        assertThat(gd.findExiledCard(firstBear.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Return from Extinction");
    }

    @Test
    void doesNotChooseReplacementTargetsWhenBothTargetsLeaveGraveyard() {
        Card firstBear = new MotherBear();
        Card secondBear = new MotherBear();
        Card unchosenBear = new MotherBear();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, unchosenBear));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.setGraveyard(player1, List.of(unchosenBear));
        harness.setExile(player1, List.of(firstBear, secondBear));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unchosenBear);
        harness.assertInGraveyard(player1, "Return from Extinction");
    }

    @Test
    void singleCardModeExcludesNoncreaturesAndOpponentsGraveyard() {
        Card bear = new MotherBear();
        Card noncreature = new ReturnFromExtinction();
        Card opponentsCreature = new ExcavatingAnurid();
        harness.setGraveyard(player1, List.of(bear, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 0, List.of());
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature);
        harness.assertInGraveyard(player2, "Excavating Anurid");
    }

    @Test
    void singleCardModeRequiresACreatureInYourGraveyard() {
        harness.setGraveyard(player1, List.of(new ReturnFromExtinction()));
        harness.setGraveyard(player2, List.of(new MotherBear()));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Return from Extinction");
    }
}
