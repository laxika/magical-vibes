package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DiregrafScavenger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RevealingEye;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConcealingCurtains.class, RevealingEye.class, Forest.class, DiregrafScavenger.class, Island.class})
class ConcealingCurtainsTest extends BaseCardTest {

    @Test
    @DisplayName("Transforming into Revealing Eye reveals an opponent's hand and offers a nonland discard")
    void transformsAndOffersNonlandDiscard() {
        harness.setHand(player2, List.of(new Forest(), new DiregrafScavenger()));
        harness.setLibrary(player2, List.of(new Island()));

        Permanent curtains = transformCurtains();

        assertThat(curtains.getCard()).isInstanceOf(RevealingEye.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
        assertThat(choice.optional()).isTrue();

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Diregraf Scavenger");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Island");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining Revealing Eye's optional discard does nothing")
    void decliningDiscardDoesNothing() {
        harness.setHand(player2, List.of(new DiregrafScavenger()));
        harness.setLibrary(player2, List.of(new Island()));

        transformCurtains();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Diregraf Scavenger");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A hand containing only lands offers no discard")
    void onlyLandsOfferNoDiscard() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        transformCurtains();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    @Test
    @DisplayName("The transform ability is restricted to sorcery speed")
    void transformAbilityIsSorcerySpeed() {
        harness.addToBattlefield(player1, new ConcealingCurtains());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The transform trigger cannot target its controller")
    void transformTriggerCannotTargetController() {
        harness.setHand(player2, List.of(new DiregrafScavenger()));
        Permanent curtains = beginTransformCurtains();

        assertThat(curtains.getCard()).isInstanceOf(RevealingEye.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent transformCurtains() {
        Permanent curtains = beginTransformCurtains();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        return curtains;
    }

    @Test
    @DisplayName("An empty revealed hand does not cause a draw")
    void emptyHandDoesNotDraw() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island()));

        transformCurtains();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Island");
    }

    @Test
    @DisplayName("A land cannot be selected from a mixed revealed hand")
    void cannotChooseLand() {
        harness.setHand(player2, List.of(new Forest(), new DiregrafScavenger()));
        harness.setLibrary(player2, List.of(new Island()));

        transformCurtains();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Forest", "Diregraf Scavenger");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The transform ability cannot be activated during combat")
    void cannotTransformOutsideMainPhase() {
        harness.addToBattlefield(player1, new ConcealingCurtains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Concealing Curtains");
    }

    @Test
    @DisplayName("The transform ability cannot be activated in response to a spell")
    void cannotTransformWithNonemptyStack() {
        harness.addToBattlefield(player1, new ConcealingCurtains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ConcealingCurtains(), "{B}");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Concealing Curtains");
    }

    private Permanent beginTransformCurtains() {
        Permanent curtains = harness.addToBattlefieldAndReturn(player1, new ConcealingCurtains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return curtains;
    }
}
