package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Guile;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenderSilent.class, LlanowarElves.class, GrizzlyBears.class, GaeasHerald.class, Cancel.class})
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
        harness.castAndResolveInstant(player2, 0, elves.getId());

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
        harness.castAndResolveInstant(player2, 0, elves.getId());

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
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void uncounterableSpellStillSilencesItsController() {
        harness.addToBattlefield(player1, new GaeasHerald());
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new RenderSilent()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingTargetDoesNotSilenceItsController() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new RenderSilent(), new Cancel()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.castAndResolveInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.playersSilencedThisTurn).doesNotContain(player1.getId());
        harness.assertInGraveyard(player2, "Render Silent");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({Guile.class})
    void guileAllowsRecastingOwnSpellBeforeRestrictionApplies() {
        harness.addToBattlefield(player1, new Guile());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new RenderSilent()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playersSilencedThisTurn).contains(player1.getId());
    }

    @Test
    void restrictionExpiresAtEndOfTurn() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves, new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new RenderSilent()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
