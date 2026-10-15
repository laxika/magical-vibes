package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.b.BattleMenu;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JenovaAncientCalamity.class, GrizzlyBears.class, DoomBlade.class, BattleMenu.class})
class JenovaAncientCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, another creature gets Jenova's power in counters and becomes a Mutant")
    void buffsAnotherCreatureAndMakesItMutant() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("During its controller's turn, Jenova draws cards equal to a dying Mutant's power")
    void drawsDyingMutantsPowerDuringControllerTurn() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.getGrantedSubtypes().add(CardSubtype.MUTANT);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Jenova does not draw when a Mutant dies during an opponent's turn")
    void doesNotDrawDuringOpponentTurn() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.getGrantedSubtypes().add(CardSubtype.MUTANT);

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void usesJenovasPowerAtResolutionAndCanTargetOpponentCreature() {
        Permanent jenova = addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        jenova.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bear.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    void zeroPowerStillMakesTargetAMutant() {
        Permanent jenova = addCreatureReady(player1, new JenovaAncientCalamity());
        jenova.setPowerModifier(-1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    void doesNotDrawForNonMutantDeath() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({JenovaAncientCalamity.class, BattleMenu.class})
    void drawsForJenovasOwnDeathWhenItIsAMutant() {
        Permanent jenova = addCreatureReady(player1, new JenovaAncientCalamity());
        jenova.getGrantedSubtypes().add(CardSubtype.MUTANT);
        jenova.setPowerModifier(3);
        harness.setHand(player1, List.of(new BattleMenu()));
        harness.setLibrary(player1, List.of(new JenovaAncientCalamity(), new JenovaAncientCalamity(),
                new JenovaAncientCalamity(), new JenovaAncientCalamity(), new JenovaAncientCalamity()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 2, jenova.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Jenova, Ancient Calamity");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void canChooseNoTargetEvenWithAnotherCreatureAvailable() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getGrantedSubtypes()).doesNotContain(CardSubtype.MUTANT);
    }

    @Test
    void doesNotDrawForOpponentMutantDeath() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.getGrantedSubtypes().add(CardSubtype.MUTANT);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsUsingDyingMutantsModifiedPower() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.getGrantedSubtypes().add(CardSubtype.MUTANT);
        bear.setPowerModifier(2);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
