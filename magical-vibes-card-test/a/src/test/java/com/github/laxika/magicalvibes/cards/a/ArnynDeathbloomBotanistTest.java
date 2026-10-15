package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArnynDeathbloomBotanist.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class ArnynDeathbloomBotanistTest extends BaseCardTest {

    @Test
    @DisplayName("When a 1/1 creature you control dies, target opponent loses 2 life and you gain 2 life")
    void smallAllyDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castAndResolveInstant(player2, 0, elvesId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not trigger when a creature without power or toughness 1 or less dies")
    void largeAllyDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Arnyn's own death at 2/2 does not trigger")
    void ownDeathAtTwoTwoDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID arnynId = harness.getPermanentId(player1, "Arnyn, Deathbloom Botanist");
        harness.castAndResolveInstant(player2, 0, arnynId);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    void allyWithOnlyLowPowerTriggers() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPowerModifier(-1);

        killWithShock(bears.getId());
        chooseOpponentAndResolveDrain();
    }

    @Test
    void allyWithOnlyLowToughnessTriggers() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setToughnessModifier(-1);

        killWithShock(bears.getId());
        chooseOpponentAndResolveDrain();
    }

    @Test
    void ownDeathWithReducedPowerTriggersExactlyOnce() {
        Permanent arnyn = harness.addToBattlefieldAndReturn(player1, new ArnynDeathbloomBotanist());
        arnyn.setPowerModifier(-1);

        killWithShock(arnyn.getId());
        chooseOpponentAndResolveDrain();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostedSmallCreatureDoesNotQualifyByPrintedStats() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setPowerModifier(1);
        elves.setToughnessModifier(1);

        killWithShock(elves.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void zeroToughnessDeathTriggersWithoutDamage() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setToughnessModifier(-2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        chooseOpponentAndResolveDrain();
    }

    @Test
    void opponentSmallCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArnynDeathbloomBotanist());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        killWithShock(elves.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killWithShock(UUID targetId) {
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }

    private void chooseOpponentAndResolveDrain() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
