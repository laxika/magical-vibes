package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandmanShiftingScoundrel.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        GarruksPackleader.class})
class SandmanShiftingScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands controlled")
    void powerAndToughnessEqualLandsControlled() {
        addLand(player1);
        addLand(player1);
        Permanent sandman = addCreatureReady(player1, new SandmanShiftingScoundrel());

        assertThat(gqs.getEffectivePower(gd, sandman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sandman)).isEqualTo(2);

        addLand(player1);

        assertThat(gqs.getEffectivePower(gd, sandman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sandman)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedBySmallCreature() {
        addLand(player1);
        addLand(player1);
        addLand(player1);
        Permanent sandman = addCreatureReady(player1, new SandmanShiftingScoundrel());
        sandman.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, sandman))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power greater than 2")
    void canBeBlockedByLargeCreature() {
        addLand(player1);
        addLand(player1);
        addLand(player1);
        addLand(player1);
        Permanent sandman = addCreatureReady(player1, new SandmanShiftingScoundrel());
        sandman.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        declareBlock(blocker, sandman);
    }

    @Test
    @DisplayName("Graveyard ability returns Sandman and a target land tapped")
    void graveyardAbilityReturnsSandmanAndTargetLandTapped() {
        SandmanShiftingScoundrel sandman = new SandmanShiftingScoundrel();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(sandman, forest));
        prepareMainPhase();
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sandman, Shifting Scoundrel").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Sandman, Shifting Scoundrel");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Graveyard ability requires a land target")
    void graveyardAbilityRequiresLandTarget() {
        SandmanShiftingScoundrel sandman = new SandmanShiftingScoundrel();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(sandman, bears));
        prepareMainPhase();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the controller's lands contribute to power and toughness")
    void opponentsLandsDoNotContribute() {
        addLand(player1);
        addLand(player2);
        addLand(player2);
        Permanent sandman = addCreatureReady(player1, new SandmanShiftingScoundrel());

        assertThat(gqs.getEffectivePower(gd, sandman)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sandman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a land target")
    void cannotActivateWithoutLandTarget() {
        harness.setGraveyard(player1, List.of(new SandmanShiftingScoundrel()));
        prepareMainPhase();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land in the opponent's graveyard")
    void cannotTargetOpponentsLand() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(new SandmanShiftingScoundrel()));
        harness.setGraveyard(player2, List.of(forest));
        prepareMainPhase();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sandman stays in the graveyard when the only target becomes illegal")
    void illegalLandTargetPreventsBothReturns() {
        SandmanShiftingScoundrel sandman = new SandmanShiftingScoundrel();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(sandman, forest));
        prepareMainPhase();
        addActivationMana();
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));

        harness.setGraveyard(player1, List.of(sandman));
        harness.setExile(player1, List.of(forest));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sandman, Shifting Scoundrel");
        harness.assertNotOnBattlefield(player1, "Sandman, Shifting Scoundrel");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The land returns even when Sandman has left the graveyard")
    void landReturnsWithoutSandman() {
        SandmanShiftingScoundrel sandman = new SandmanShiftingScoundrel();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(sandman, forest));
        prepareMainPhase();
        addActivationMana();
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));

        harness.setGraveyard(player1, List.of(forest));
        harness.setExile(player1, List.of(sandman));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sandman, Shifting Scoundrel");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An older activation cannot return Sandman after it returns and dies again")
    void olderActivationDoesNotReturnNewGraveyardObject() {
        SandmanShiftingScoundrel sandman = new SandmanShiftingScoundrel();
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setGraveyard(player1, List.of(sandman, firstLand, secondLand));
        prepareMainPhase();
        addActivationMana();
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(firstLand.getId()));
        addActivationMana();
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(secondLand.getId()));
        harness.passBothPriorities();

        Permanent returnedSandman = findPermanent(player1, "Sandman, Shifting Scoundrel");
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, returnedSandman);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sandman, Shifting Scoundrel");
        harness.assertNotOnBattlefield(player1, "Sandman, Shifting Scoundrel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(firstLand.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("The returning land contributes to Sandman's power for entry triggers")
    void landAndSandmanEnterSimultaneously() {
        addLand(player1);
        addLand(player1);
        harness.addToBattlefield(player1, new GarruksPackleader());
        SandmanShiftingScoundrel sandman = new SandmanShiftingScoundrel();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(sandman, forest));
        prepareMainPhase();
        addActivationMana();
        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Sandman, Shifting Scoundrel")))
                .isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).isEmpty();
    }

    private void addLand(Player player) {
        harness.addToBattlefield(player, new Forest());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
