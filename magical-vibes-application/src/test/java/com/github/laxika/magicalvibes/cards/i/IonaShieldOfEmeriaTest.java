package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VaporSnag;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IonaShieldOfEmeriaTest extends BaseCardTest {

    private Permanent addIona(CardColor chosenColor) {
        Permanent iona = harness.addToBattlefieldAndReturn(player1, new IonaShieldOfEmeria());
        iona.setChosenColor(chosenColor);
        return iona;
    }

    private UUID addTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        return harness.getPermanentId(player1, "Grizzly Bears");
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent cannot cast a spell of the chosen color")
    void opponentCannotCastChosenColorSpell() {
        addIona(CardColor.BLUE);
        harness.setHand(player2, List.of(new VaporSnag()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID targetId = addTarget();
        prepareOpponentTurn();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent can cast a spell of another color")
    void opponentCanCastAnotherColorSpell() {
        addIona(CardColor.BLUE);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID targetId = addTarget();
        prepareOpponentTurn();

        harness.castInstant(player2, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Iona's controller can cast a spell of the chosen color")
    void controllerCanCastChosenColorSpell() {
        addIona(CardColor.BLUE);
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Casting Iona prompts for the color and applies the restriction")
    void castAndChooseColor() {
        harness.setHand(player1, List.of(new IonaShieldOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player2, List.of(new VaporSnag()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID targetId = addTarget();
        prepareOpponentTurn();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
