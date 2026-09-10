package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConcealingCurtains.class, RevealingEye.class, Forest.class, GrizzlyBears.class, Island.class})
class ConcealingCurtainsTest extends BaseCardTest {

    @Test
    @DisplayName("Transforming into Revealing Eye reveals an opponent's hand and offers a nonland discard")
    void transformsAndOffersNonlandDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        harness.setLibrary(player2, List.of(new Island()));

        Permanent curtains = transformCurtains();

        assertThat(curtains.getCard()).isInstanceOf(RevealingEye.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);
        assertThat(choice.optional()).isTrue();

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Island");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining Revealing Eye's optional discard does nothing")
    void decliningDiscardDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player2, List.of(new Island()));

        transformCurtains();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A hand containing only lands offers no discard")
    void onlyLandsOfferNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
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
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
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

    private Permanent beginTransformCurtains() {
        Permanent curtains = harness.addToBattlefieldAndReturn(player1, new ConcealingCurtains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        return curtains;
    }
}
