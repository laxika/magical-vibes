package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumPrelate.class, Shock.class, GrizzlyBears.class, Fireball.class, TurnToFrog.class})
class SanctumPrelateTest extends BaseCardTest {

    private void castPrelate(int chosenNumber) {
        harness.setHand(player1, List.of(new SanctumPrelate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, Integer.toString(chosenNumber));
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("As Sanctum Prelate enters, choosing a number is required")
    void enteringRequiresNumberChoice() {
        harness.setHand(player1, List.of(new SanctumPrelate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "1");
    }

    @Test
    @DisplayName("Blocks matching noncreature spells for every player")
    void blocksMatchingNoncreatureSpellsForEveryPlayer() {
        castPrelate(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareOpponentMainPhase();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not block creature spells")
    void allowsCreatureSpells() {
        castPrelate(2);
        prepareOpponentMainPhase();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not block nonmatching noncreature spells")
    void allowsNonmatchingNoncreatureSpells() {
        castPrelate(2);
        prepareOpponentMainPhase();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Uses the chosen X value when checking a spell's mana value")
    void checksChosenXValue() {
        castPrelate(2);
        prepareOpponentMainPhase();

        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.RED, 1);
        harness.castSorcery(player2, 0, 2, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A number greater than twenty can be chosen")
    void allowsChoosingNumberGreaterThanTwenty() {
        harness.setHand(player1, List.of(new SanctumPrelate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .contains("21");
        harness.handleListChoice(player1, "21");
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 21);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 20, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Choosing zero permits a spell with a positive mana value")
    void choosingZeroAllowsShock() {
        castPrelate(0);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Removing Sanctum Prelate ends its restriction")
    void restrictionEndsWhenPrelateLeaves() {
        castPrelate(2);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Shock(), new Fireball()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Sanctum Prelate"));
        harness.assertNotOnBattlefield(player1, "Sanctum Prelate");
        harness.castAndResolveSorcery(player2, 0, 1, player1.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Losing all abilities ends the casting restriction")
    void restrictionDoesNotApplyAfterLosingAbilities() {
        castPrelate(1);
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Sanctum Prelate"));
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }
}
