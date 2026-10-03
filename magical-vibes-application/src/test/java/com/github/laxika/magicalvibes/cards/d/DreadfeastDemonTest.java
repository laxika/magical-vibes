package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadfeastDemon.class, PersistentSpecimen.class})
class DreadfeastDemonTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of the end step, it sacrifices a non-Demon creature and creates a token copy")
    void sacrificesNonDemonCreatureAndCreatesTokenCopy() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DreadfeastDemon());
        Permanent specimen = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(specimen.getId());

        harness.handlePermanentChosen(player1, specimen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(demon);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(specimen.getCard());
    }

    @Test
    @DisplayName("It does not trigger a sacrifice when you control no non-Demon creature")
    void ignoresDemonCreatures() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DreadfeastDemon());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(demon);
    }

    @Test
    @DisplayName("The copy is created during the sacrifice ability's resolution without another priority window")
    void createsCopyBeforePlayersReceivePriorityAfterSacrifice() {
        harness.addToBattlefield(player1, new DreadfeastDemon());
        Permanent specimen = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            advanceToEndStep(player1);
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, specimen.getId());

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(permanent -> permanent.getCard().isToken())
                    .hasSize(1);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DreadfeastDemon());
        Permanent specimen = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            advanceToEndStep(player2);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(demon, specimen);
        });
    }

    @Test
    @DisplayName("An opponent's non-Demon creature cannot be sacrificed")
    void cannotSacrificeOpponentsCreature() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DreadfeastDemon());
        Permanent specimen = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            advanceToEndStep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(demon);
            assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(specimen);
        });
    }

    @Test
    @DisplayName("A token copy retains the end-step ability but does not trigger in the end step it enters")
    void tokenCopyTriggersAtALaterEndStep() {
        harness.addToBattlefield(player1, new DreadfeastDemon());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();

        Permanent second = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(3);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
