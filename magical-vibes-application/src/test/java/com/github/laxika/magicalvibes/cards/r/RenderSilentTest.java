package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RenderSilentTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and silences its controller")
    void countersAndSilencesController() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new RenderSilent()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playersSilencedThisTurn).contains(player1.getId());
        assertThat(gd.playersSilencedThisTurn).doesNotContain(player2.getId());
    }

    @Test
    @DisplayName("The silenced controller cannot cast another spell this turn")
    void silencedControllerCannotCastAgain() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new RenderSilent()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster of Render Silent may still cast spells")
    void casterIsNotSilenced() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new RenderSilent(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
