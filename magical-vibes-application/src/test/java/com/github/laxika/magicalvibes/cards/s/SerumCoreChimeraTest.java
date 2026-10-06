package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KothFireOfResistance;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerumCoreChimera.class, Shock.class, GrizzlyBears.class, Forest.class})
class SerumCoreChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Serum-Core Chimera")
    void noncreatureSpellPutsOilCounter() {
        Permanent chimera = addReadyChimera(0);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Serum-Core Chimera")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent chimera = addReadyChimera(0);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Removing three oil counters draws, discards a nonland, and deals 3 damage")
    void removesOilCountersDrawsDiscardsAndDealsDamage() {
        Permanent chimera = addReadyChimera(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerumCoreChimera());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the discard still draws a card but deals no damage")
    void mayDeclineDiscard() {
        Permanent chimera = addReadyChimera(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerumCoreChimera());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The reflexive damage trigger cannot target a player")
    void reflexiveDamageCannotTargetPlayer() {
        addReadyChimera(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The oil counters are paid immediately and insufficient counters prevent activation")
    void requiresThreeOilCountersAsCost() {
        Permanent chimera = addReadyChimera(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();

        chimera.setCounterCount(CounterType.OIL, 4);
        harness.activateAbility(player1, 0, null, null);
        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability cannot be used outside a main phase")
    void cannotActivateDuringCombat() {
        Permanent chimera = addReadyChimera(3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability cannot be used during another player's turn")
    void cannotActivateOnOpponentsTurn() {
        Permanent chimera = addReadyChimera(3);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability cannot be used while the stack is nonempty")
    void cannotActivateInResponse() {
        Permanent chimera = addReadyChimera(3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chimera.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick Chimera can activate and discard the card it just drew")
    void canDiscardDrawnCardWhileSummoningSick() {
        Permanent chimera = addReadyChimera(3);
        chimera.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SerumCoreChimera()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Serum-Core Chimera");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, chimera.getId());

        assertThat(chimera.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(chimera.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Serum-Core Chimera");
    }

    @Test
    @DisplayName("A land cannot be discarded for the damage trigger")
    void cannotDiscardLand() {
        addReadyChimera(3);
        harness.setHand(player1, List.of(new Forest(), new SerumCoreChimera()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not add oil")
    void opponentsSpellDoesNotAddOil() {
        Permanent chimera = addReadyChimera(0);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(chimera.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @CardUsed(KothFireOfResistance.class)
    @DisplayName("The reflexive trigger can deal damage to a planeswalker")
    void damagesPlaneswalker() {
        addReadyChimera(3);
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothFireOfResistance());
        koth.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new SerumCoreChimera()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, koth.getId());
        harness.passBothPriorities();
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The reflexive damage trigger cannot target a land")
    void reflexiveDamageCannotTargetLand() {
        addReadyChimera(3);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SerumCoreChimera()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChimera(int oilCounters) {
        Permanent chimera = harness.addToBattlefieldAndReturn(player1, new SerumCoreChimera());
        chimera.setSummoningSick(false);
        chimera.setCounterCount(CounterType.OIL, oilCounters);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return chimera;
    }
}
