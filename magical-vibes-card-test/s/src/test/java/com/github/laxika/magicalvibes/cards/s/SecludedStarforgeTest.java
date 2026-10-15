package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecludedStarforge.class, GrizzlyBears.class, Ornithopter.class})
class SecludedStarforgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Secluded Starforge produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent starforge = addStarforge(player1);

        harness.activateAbility(player1, battlefieldIndex(starforge), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(starforge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pumping a creature taps X untapped artifacts and lasts until end of turn")
    void pumpsCreatureForXAndTapsArtifacts() {
        Permanent starforge = addStarforge(player1);
        Permanent artifact1 = addArtifact(player1);
        Permanent artifact2 = addArtifact(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(starforge), 1, 2, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(starforge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pumping cannot be activated outside sorcery timing")
    void pumpingIsSorcerySpeed() {
        Permanent starforge = addStarforge(player1);
        addArtifact(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(starforge), 1, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying {5} and tapping Secluded Starforge creates a Robot token")
    void createsRobotToken() {
        Permanent starforge = addStarforge(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, battlefieldIndex(starforge), 2, null, null);
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().getPower()).isEqualTo(2);
        assertThat(robot.getCard().getToughness()).isEqualTo(2);
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(starforge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pumping cannot activate without enough untapped artifacts")
    void pumpingRequiresEnoughUntappedArtifacts() {
        Permanent starforge = addStarforge(player1);
        addArtifact(player1).tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(starforge), 1, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(starforge.isTapped()).isFalse();
    }

    @Test
    void zeroXNeedsNoArtifactsButStillPaysManaAndTapsTheLand() {
        Permanent starforge = addStarforge(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(starforge), 1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(starforge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void newlyEnteredArtifactCreatureCanPayTheCostAndBeTheTarget() {
        Permanent starforge = addStarforge(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(true);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(starforge), 1, 1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
    }

    @Test
    void choosingOneArtifactLeavesOtherArtifactsUntapped() {
        Permanent starforge = addStarforge(player1);
        Permanent chosen = addArtifact(player1);
        Permanent other = addArtifact(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(starforge), 1, 1, other.getId());
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(chosen.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        assertThat(starforge.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void opponentsArtifactsCannotPayTheCost() {
        Permanent starforge = addStarforge(player1);
        Permanent artifact = addArtifact(player2);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(starforge), 1, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(starforge.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void powerBoostExpiresAtTurnCleanup() {
        Permanent starforge = addStarforge(player1);
        Permanent artifact = addArtifact(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(starforge), 1, 1, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, artifact)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(2);
    }

    @Test
    void robotCreationCanBeActivatedOnOpponentsTurn() {
        Permanent starforge = addStarforge(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, battlefieldIndex(starforge), 2, null, null);
        assertThat(countPermanents(player1, "Robot")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Robot")).isEqualTo(1);
        assertThat(countPermanents(player2, "Robot")).isZero();
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.ROBOT);
        assertThat(robot.isTapped()).isFalse();
    }

    @Test
    void pumpingCannotBeActivatedWithAnAbilityOnTheStack() {
        Permanent producer = addStarforge(player1);
        Permanent starforge = addStarforge(player1);
        Permanent creature = addArtifact(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, battlefieldIndex(producer), 2, null, null);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(starforge), 1, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(starforge.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addStarforge(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new SecludedStarforge());
    }

    private Permanent addArtifact(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new Ornithopter());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
