package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Extruder.class, BraidwoodCup.class, GoliathBeetle.class})
class ExtruderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact puts a +1/+1 counter on target creature")
    void sacrificingArtifactPutsCounterOnTargetCreature() {
        addCreatureReady(player1, new Extruder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Braidwood Cup");
        harness.assertOnBattlefield(player1, "Extruder");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new Extruder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The ability may sacrifice Extruder itself and still resolve")
    void sacrificingExtruderItselfStillResolvesAbility() {
        addCreatureReady(player1, new Extruder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Extruder");
    }

    @Test
    @DisplayName("The ability can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new Extruder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining echo sacrifices Extruder at its next upkeep")
    void decliningEchoSacrificesExtruder() {
        castAndResolveExtruder();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Extruder");
        harness.assertInGraveyard(player1, "Extruder");
    }

    @Test
    @DisplayName("Paying echo keeps Extruder and echo does not trigger again")
    void payingEchoKeepsExtruderAndIsOneShot() {
        castAndResolveExtruder();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Extruder");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Extruder");
    }

    @Test
    @DisplayName("Echo does not create an enters-the-battlefield trigger")
    void echoDoesNotTriggerOnEntry() {
        harness.castFromHand(player1, new Extruder(), "{4}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Extruder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Extruder can activate its ability")
    void tappedSummoningSickExtruderCanActivate() {
        Permanent extruder = harness.addToBattlefieldAndReturn(player1, new Extruder());
        extruder.setSummoningSick(true);
        extruder.setTapped(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.assertInGraveyard(player1, "Extruder");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the targeted Extruder leaves no legal target")
    void sacrificingTargetedExtruderDoesNotPutCounterOnAnotherCreature() {
        Permanent extruder = addCreatureReady(player1, new Extruder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());

        harness.activateAbility(player1, 0, 0, null, extruder.getId());
        harness.assertInGraveyard(player1, "Extruder");
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveExtruder() {
        harness.castFromHand(player1, new Extruder(), "{4}");
        resolveAllTriggers();
    }
}
