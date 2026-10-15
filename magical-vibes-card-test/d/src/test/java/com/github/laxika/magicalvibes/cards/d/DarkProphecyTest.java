package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.h.HiveStirrings;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkProphecy.class, ChildOfNight.class, Shock.class, Naturalize.class,
        Opalescence.class, PlanarCleansing.class, HiveStirrings.class})
class DarkProphecyTest extends BaseCardTest {

    // "Whenever a creature you control dies, you draw a card and you lose 1 life."

    /** Player1 shocks a creature; resolve Shock, the death, then the death triggers. */
    private void killWithShock(com.github.laxika.magicalvibes.model.Player owner, String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(owner, targetName);
        harness.castAndResolveInstant(player1, 0, targetId);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Your creature dying draws a card and costs 1 life")
    void ownCreatureDeathDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new ChildOfNight());

        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player1, "Child of Night");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // Shock was cast, one card drawn
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player2, new ChildOfNight());

        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player2, "Child of Night");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Drawing and losing life resolve together in one mandatory trigger")
    void drawAndLifeLossResolveTogether() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new ChildOfNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Child of Night"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Child of Night");
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Each Dark Prophecy triggers independently for the same death")
    void multipleCopiesTriggerIndependently() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new ChildOfNight(), new ChildOfNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Child of Night"));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore - 1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A death trigger still resolves after Dark Prophecy is destroyed")
    void triggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of(new Shock(), new Naturalize()));
        harness.setLibrary(player1, List.of(new ChildOfNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Child of Night"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dark Prophecy"));

        harness.assertInGraveyard(player1, "Dark Prophecy");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Child of Night");
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("An animated Dark Prophecy triggers when it dies itself")
    void animatedProphecyTriggersForItsOwnDeath() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.setLibrary(player1, List.of(new ChildOfNight()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dark Prophecy"));

        harness.assertInGraveyard(player1, "Dark Prophecy");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Child of Night");
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once per controlled creature even when Dark Prophecy also leaves")
    void simultaneousDeathsWithSourceDestruction() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.addToBattlefield(player2, new ChildOfNight());
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.setLibrary(player1, List.of(new ChildOfNight(), new ChildOfNight()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Dark Prophecy");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore - 2);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature token deaths also draw a card and lose life")
    void tokenDeathTriggers() {
        harness.addToBattlefield(player1, new DarkProphecy());
        harness.setHand(player1, List.of(new HiveStirrings()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(new ChildOfNight()));
        int lifeBefore = gd.getLife(player1.getId());

        killWithShock(player1, "Sliver");

        harness.assertInHand(player1, "Child of Night");
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }
}
