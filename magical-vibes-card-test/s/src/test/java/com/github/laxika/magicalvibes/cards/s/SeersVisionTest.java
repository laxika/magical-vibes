package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeersVision.class, DreamThrush.class, Opt.class})
class SeersVisionTest extends BaseCardTest {

    @Test
    @DisplayName("The controller sees opponents' hands, but opponents do not see the controller's hand")
    void onlyOpponentsHandsAreRevealed() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player1, List.of(new DreamThrush()));
        harness.setHand(player2, List.of(new Opt()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Opt"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\":[]"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"opponentHand\"")
                        && message.contains("Dream Thrush"));
    }

    @Test
    @DisplayName("Sacrificing Seer's Vision lets its controller choose a card for the target to discard")
    void sacrificeAndDiscardChosenCard() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player2, List.of(new DreamThrush(), new Opt()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Seer's Vision");
        harness.assertInGraveyard(player1, "Seer's Vision");

        harness.clearMessages();
        harness.passBothPriorities();

        assertThat(gd.gameLog)
                .anyMatch(log -> log.plainText().contains("looks at " + player2.getUsername() + "'s hand."));
        assertThat(gd.gameLog)
                .noneMatch(log -> log.plainText().contains("reveals their hand")
                        && log.plainText().contains("Opt"));
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Opt"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Opt");
        harness.assertInHand(player2, "Dream Thrush");
    }

    @Test
    @DisplayName("Sacrificing Seer's Vision does not create a choice when the target has an empty hand")
    void sacrificeWithEmptyTargetHand() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Seer's Vision");
        harness.assertInGraveyard(player1, "Seer's Vision");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated outside its controller's main phase")
    void abilityRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Seer's Vision");
    }

    @Test
    @DisplayName("The controller can target their own hand and choose a card to discard")
    void canTargetOwnHand() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player1, List.of(new DreamThrush(), new Opt()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Seer's Vision");
        harness.assertInGraveyard(player1, "Dream Thrush");
        harness.assertInHand(player1, "Opt");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability cannot be activated during combat even on its controller's turn")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Seer's Vision");
        harness.assertNotInGraveyard(player1, "Seer's Vision");
    }

    @Test
    @DisplayName("The ability cannot be activated while another spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Seer's Vision");
        harness.assertNotInGraveyard(player1, "Seer's Vision");
    }

    @Test
    @DisplayName("Opponents' hands stop being revealed as soon as the enchantment is sacrificed")
    void sacrificeEndsContinuousRevealBeforeResolution() {
        harness.addToBattlefield(player1, new SeersVision());
        harness.setHand(player2, List.of(new DreamThrush()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"opponentHand\":[]"));
        assertThat(harness.getConn1().getSentMessages())
                .noneMatch(message -> message.contains("Dream Thrush"));
        harness.assertInHand(player2, "Dream Thrush");
    }
}
