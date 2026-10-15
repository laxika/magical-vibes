package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Goremand.class, LlanowarElves.class, GrizzlyBears.class})
@DisplayName("Goremand")
class GoremandTest extends BaseCardTest {

    @Test
    @DisplayName("Casting sacrifices a creature as an additional cost")
    void castingSacrificesCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("ETB makes each opponent sacrifice a creature")
    void etbMakesEachOpponentSacrifice() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goremand");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent chooses exactly one creature and controller keeps their remaining creatures")
    void opponentChoosesOneCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.assertOnBattlefield(player1, "Goremand");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent without creatures does not prevent Goremand from resolving")
    void resolvesWithoutOpponentCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goremand");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without casting still triggers the opponent sacrifice without a casting cost")
    void enteringWithoutCastingTriggersSacrifice() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new Goremand());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goremand");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new Goremand()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
