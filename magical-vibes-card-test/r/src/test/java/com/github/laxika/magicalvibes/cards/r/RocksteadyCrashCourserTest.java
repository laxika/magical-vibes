package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CurseOfTheSwine;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RocksteadyCrashCourser.class, GrizzlyBears.class, CurseOfTheSwine.class, Forest.class, Mountain.class})
class RocksteadyCrashCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Rocksteady can't be blocked by more than one creature")
    void rocksteadyCannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new RocksteadyCrashCourser());
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Boars you control can't be blocked by more than one creature")
    void controlledBoarCannotBeBlockedByTwoCreatures() {
        Permanent boar = createBoar();
        addCreatureReady(player1, new RocksteadyCrashCourser());
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        boar.setAttacking(true);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(boar);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Creatures you control that aren't Boars can still be blocked by two creatures")
    void nonBoarCanBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new RocksteadyCrashCourser());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerOne), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blockerTwo), attackerIndex)
        ));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestcycling discards Rocksteady and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new RocksteadyCrashCourser()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rocksteady, Crash Courser");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Forest)
                .hasSize(2);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    private Permanent createBoar() {
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CurseOfTheSwine()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, victim.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Boar".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void rocksteadyCanBeBlockedByOneCreature() {
        Permanent attacker = addCreatureReady(player1, new RocksteadyCrashCourser());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void controlledBoarCanBeBlockedByOneCreature() {
        Permanent boar = createBoar();
        addCreatureReady(player1, new RocksteadyCrashCourser());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        boar.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                0, gd.playerBattlefields.get(player1.getId()).indexOf(boar))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void opposingRocksteadyDoesNotRestrictBoars() {
        Permanent boar = createBoar();
        addCreatureReady(player2, new RocksteadyCrashCourser());
        Permanent blockerOne = addCreatureReady(player2, new GrizzlyBears());
        Permanent blockerTwo = addCreatureReady(player2, new GrizzlyBears());
        boar.setAttacking(true);
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(boar);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, attackerIndex),
                new BlockerAssignment(2, attackerIndex)));

        assertThat(blockerOne.isBlocking()).isTrue();
        assertThat(blockerTwo.isBlocking()).isTrue();
    }

    @Test
    void forestcyclingPaysAndDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new RocksteadyCrashCourser()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Rocksteady, Crash Courser");
        harness.assertInGraveyard(player1, "Rocksteady, Crash Courser");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void forestcyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new RocksteadyCrashCourser()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Rocksteady, Crash Courser");
        harness.assertNotInGraveyard(player1, "Rocksteady, Crash Courser");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void forestcyclingCanFailToFindEvenWithForestAvailable() {
        harness.setHand(player1, List.of(new RocksteadyCrashCourser()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Rocksteady, Crash Courser");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void forestcyclingWithNoForestDoesNotFindOtherLands() {
        harness.setHand(player1, List.of(new RocksteadyCrashCourser()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Rocksteady, Crash Courser");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
