package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousSnare.class, GrizzlyBears.class, DuskLegionDreadnought.class, Disperse.class, Forest.class})
class PerilousSnareTest extends BaseCardTest {

    @Test
    void exilesTargetNonlandPermanentAnOpponentControlsUntilItLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSnare(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent snare = findPermanent(player1, "Perilous Snare");
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, snare.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSnareCast();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void maxSpeedAbilityPutsCountersOnCreatureAndVehicleYouControl() {
        Permanent firstSnare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent secondSnare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 3, null, vehicle.getId());
        harness.passBothPriorities();
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstSnare.isTapped()).isTrue();
        assertThat(secondSnare.isTapped()).isTrue();
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        Permanent snare = addCreatureReady(player1, new PerilousSnare());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(snare.isTapped()).isFalse();
    }

    @Test
    void startsEnginesAndResolvesWithoutLegalExileTargets() {
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PerilousSnare(), "{2}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Perilous Snare");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void enteringAnotherSnareDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PerilousSnare(), "{2}{W}");
        resolveAllTriggers();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void exilesAnUncrewedVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        castSnare(vehicle.getId());

        harness.assertNotOnBattlefield(player2, "Dusk Legion Dreadnought");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(vehicle.getCard().getId()));
    }

    @Test
    void cannotExileOwnPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSnareCast();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void sourceLeavingBeforeExileResolvesDoesNotExileTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSnareCast();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent snare = findPermanent(player1, "Perilous Snare");
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, snare.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Perilous Snare");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void targetLeavingBeforeExileResolvesIsNotExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSnareCast();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Perilous Snare");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void speedThreeDoesNotAllowCounterAbility() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(snare.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterAbilityCannotTargetOpponentCreatureOrNonvehicleArtifact() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snare.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snare.isTapped()).isFalse();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterAbilityCannotActivateOutsideMainPhase() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snare.isTapped()).isFalse();
    }

    @Test
    void counterAbilityCannotActivateDuringOpponentsMainPhase() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snare.isTapped()).isFalse();
    }

    @Test
    void counterAbilityCannotActivateWithSpellOnStack() {
        Permanent snare = harness.addToBattlefieldAndReturn(player1, new PerilousSnare());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, opponent.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(snare.isTapped()).isFalse();
        resolveAllTriggers();
    }

    private void castSnare(UUID targetId) {
        prepareSnareCast();
        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareSnareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PerilousSnare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
