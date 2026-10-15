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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrinTolarianArchmage.class, Forest.class, GrizzlyBears.class,
        Island.class, JaceMemoryAdept.class, Unsummon.class})
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
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceMemoryAdept());
        jace.setCounterCount(CounterType.LOYALTY, 4);

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

        harness.castAndResolveInstant(player1, 0, bear.getId());
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

        harness.castAndResolveInstant(player1, 0, firstBear.getId());
        harness.castAndResolveInstant(player1, 0, secondBear.getId());

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

        harness.castAndResolveInstant(player1, 0, bear.getId());

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

        harness.castAndResolveInstant(player1, 0, bear.getId());

        advanceToEndStep(player2);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Barrin's own ETB return enables the end-step draw")
    void ownEtbEnablesDraw() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        castBarrin(bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returning a permanent after the end step begins does not trigger Barrin")
    void returnDuringEndStepDoesNotTrigger() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BarrinTolarianArchmage());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Unsummon()));

        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The returned permanent need not remain in hand")
    void drawsAfterReturnedCreatureIsRecast() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BarrinTolarianArchmage());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Grizzly Bears");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The end-step draw resolves even if Barrin leaves in response")
    void drawResolvesAfterBarrinLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent barrin = harness.addToBattlefieldAndReturn(player1, new BarrinTolarianArchmage());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, barrin.getId());
        harness.assertNotOnBattlefield(player1, "Barrin, Tolarian Archmage");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
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
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
