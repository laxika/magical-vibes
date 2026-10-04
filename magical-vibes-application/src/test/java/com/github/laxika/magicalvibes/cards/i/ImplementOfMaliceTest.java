package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImplementOfMalice.class, GrizzlyBears.class, Shatter.class})
class ImplementOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card")
    void targetPlayerDiscardsACard() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Implement of Malice");
    }

    @Test
    @DisplayName("Draws a card when it is put into a graveyard from the battlefield")
    void drawsWhenPutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Implement of Malice"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated only as a sorcery")
    void cannotActivateOutsideSorceryTiming() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Self-targeting draws before discarding, even with an initially empty hand")
    void drawsBeforeDiscardingWhenTargetingSelf() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.setHand(player1, List.of());
        ImplementOfMalice drawnCard = new ImplementOfMalice();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player1.getId());

        harness.assertNotOnBattlefield(player1, "Implement of Malice");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard).hasSize(2);
    }

    @Test
    @DisplayName("An empty-handed target does not prevent the controller from drawing")
    void emptyHandedTargetStillAllowsDraw() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        ImplementOfMalice drawnCard = new ImplementOfMalice();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Implement of Malice");
    }

    @Test
    @DisplayName("Cannot activate during combat on its controller's turn")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Implement of Malice");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.setHand(player1, List.of(new ImplementOfMalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Implement of Malice");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Insufficient black mana does not sacrifice the artifact")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new ImplementOfMalice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Implement of Malice");
        harness.assertNotInGraveyard(player1, "Implement of Malice");
        assertThat(gd.stack).isEmpty();
    }
}
