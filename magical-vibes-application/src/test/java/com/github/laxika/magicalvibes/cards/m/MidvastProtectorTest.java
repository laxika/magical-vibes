package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MidvastProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants the targeted creature you control protection from the chosen color")
    void etbGrantsProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, bearsId);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("The ETB fizzles when its target leaves the battlefield before resolution")
    void etbFizzlesWhenTargetGone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, bearsId);
        harness.passBothPriorities(); // resolve the creature spell, ETB trigger goes on the stack

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities(); // ETB fizzles, so no color is chosen

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("The ETB cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MidvastProtector()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
