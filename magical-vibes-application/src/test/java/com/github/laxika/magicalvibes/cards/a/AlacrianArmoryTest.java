package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BulwarkOx;
import com.github.laxika.magicalvibes.cards.c.ClamorousIronclad;
import com.github.laxika.magicalvibes.cards.l.LeoninSurveyor;
import com.github.laxika.magicalvibes.cards.t.TezzeretAgentOfBolas;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlacrianArmory.class, BulwarkOx.class, ClamorousIronclad.class, LeoninSurveyor.class,
        TezzeretAgentOfBolas.class})
class AlacrianArmoryTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1 and vigilance")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent ownCreature = addCreatureReady(player1, new LeoninSurveyor());
        Permanent opponentCreature = addCreatureReady(player2, new LeoninSurveyor());

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat saddles a targeted Mount until end of turn")
    void saddlesMount() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent mount = addCreatureReady(player1, new BulwarkOx());

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, mount.getId());
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat animates a targeted Vehicle and only permits your Vehicles or Mounts")
    void animatesVehicleAndRestrictsTargets() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent ownVehicle = addVehicle(player1);
        Permanent opponentVehicle = addVehicle(player2);

        advanceToBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownVehicle.getId()).doesNotContain(opponentVehicle.getId());

        harness.handlePermanentChosen(player1, ownVehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, ownVehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownVehicle)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownVehicle)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.VIGILANCE)).isTrue();
        assertThat(ownVehicle.isSaddled()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat target may be declined")
    void mayDeclineTarget() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent vehicle = addVehicle(player1);

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(vehicle.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Animation preserves an existing base power and toughness setting")
    void animationPreservesExistingBasePowerAndToughness() {
        Permanent tezzeret = harness.addToBattlefieldAndReturn(player1, new TezzeretAgentOfBolas());
        tezzeret.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent vehicle = addVehicle(player1);
        harness.addToBattlefield(player1, new AlacrianArmory());

        harness.activateAbility(player1, 0, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);

        advanceToBeginningOfCombat();
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);
    }

    @Test
    @DisplayName("Armory does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent vehicle = addVehicle(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("Combat trigger resolves with no target when no Mount or Vehicle is available")
    void resolvesWithoutLegalTargets() {
        harness.addToBattlefield(player1, new AlacrianArmory());
        Permanent creature = addCreatureReady(player1, new LeoninSurveyor());
        Permanent opposingMount = addCreatureReady(player2, new BulwarkOx());
        Permanent opposingVehicle = addVehicle(player2);

        advanceToBeginningOfCombat();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> harness.passBothPriorities());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isSaddled()).isFalse();
        assertThat(opposingMount.isSaddled()).isFalse();
        assertThat(gqs.isCreature(gd, opposingVehicle)).isFalse();
    }

    private Permanent addVehicle(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ClamorousIronclad());
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
