package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceMemoryAdept;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarrinTolarianArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target creature to its owner's hand")
    void etbReturnsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBarrin(bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns a target planeswalker to its owner's hand")
    void etbReturnsPlaneswalker() {
        Permanent jace = new Permanent(new JaceMemoryAdept());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(jace);

        castBarrin(jace.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(jace.getId()));
        harness.assertInHand(player2, "Jace, Memory Adept");
    }

    @Test
    @DisplayName("ETB may choose no target")
    void etbMayChooseNoTarget() {
        harness.setHand(player1, List.of(new BarrinTolarianArchmage()));
        addBarrinMana();

        harness.castCreature(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Barrin, Tolarian Archmage");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature nonplaneswalker permanent")
    void etbRejectsNonCreatureNonPlaneswalker() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new BarrinTolarianArchmage()));
        addBarrinMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Draws at your end step when a permanent was returned before Barrin entered")
    void drawsAfterPermanentReturnedBeforeEntering() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new BarrinTolarianArchmage());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws only one card after multiple permanents are returned")
    void drawsOnlyOnceAfterMultipleReturns() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BarrinTolarianArchmage());
        harness.setHand(player1, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castInstant(player1, 0, firstBear.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, secondBear.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a permanent is returned to an opponent's hand")
    void doesNotDrawForOpponentsHand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BarrinTolarianArchmage());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not draw during an opponent's end step")
    void doesNotDrawAtOpponentsEndStep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BarrinTolarianArchmage());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_STEP));
        advanceToEndStep(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castBarrin(UUID targetId) {
        harness.setHand(player1, List.of(new BarrinTolarianArchmage()));
        addBarrinMana();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addBarrinMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
