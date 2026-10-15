package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SewerCrocodile.class, AirElemental.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class SewerCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Five distinct mana values reduce Sewer Crocodile's activation cost")
    void distinctManaValuesReduceActivationCost() {
        Permanent crocodile = addReadyCrocodile();
        harness.setGraveyard(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(crocodile.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Duplicate mana values do not satisfy Sewer Crocodile's cost reduction")
    void duplicateManaValuesDoNotReduceActivationCost() {
        addReadyCrocodile();
        harness.setGraveyard(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sewer Crocodile's unblockable ability wears off at cleanup")
    void unblockableAbilityWearsOffAtCleanup() {
        Permanent crocodile = addReadyCrocodile();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(crocodile.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crocodile.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("An opponent's graveyard does not reduce the activation cost")
    void opponentsGraveyardDoesNotReduceActivationCost() {
        Permanent crocodile = addReadyCrocodile();
        harness.setGraveyard(player2, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(crocodile.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The discounted ability still requires blue mana")
    void discountedAbilityStillRequiresBlueMana() {
        Permanent crocodile = addReadyCrocodile();
        harness.setGraveyard(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(crocodile.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sewer Crocodile can activate its ability")
    void tappedSummoningSickCrocodileCanActivateAbility() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new SewerCrocodile());
        crocodile.setSummoningSick(true);
        crocodile.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(crocodile.isCantBeBlocked()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(crocodile.isCantBeBlocked()).isTrue();
        assertThat(crocodile.isTapped()).isTrue();
    }

    @Test
    @DisplayName("More than five mana values still reduce the activation cost")
    void moreThanFiveManaValuesReduceActivationCost() {
        Permanent crocodile = addReadyCrocodile();
        harness.setGraveyard(player1, List.of(new Forest(), new LlanowarElves(),
                new GrizzlyBears(), new HillGiant(), new AirElemental(), new SewerCrocodile()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(crocodile.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Losing the graveyard threshold after activation does not stop the ability")
    void graveyardThresholdIsCheckedAtActivation() {
        Permanent crocodile = addReadyCrocodile();
        harness.setGraveyard(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(), new HillGiant(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(crocodile.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Resolving the ability prevents an opponent from blocking Sewer Crocodile")
    void resolvedAbilityPreventsBlocking() {
        addReadyCrocodile();
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent addReadyCrocodile() {
        return addCreatureReady(player1, new SewerCrocodile());
    }
}
