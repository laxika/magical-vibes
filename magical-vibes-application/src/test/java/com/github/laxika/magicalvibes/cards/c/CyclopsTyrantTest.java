package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SliverConstruct;
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

@CardUsed({CyclopsTyrant.class, GrizzlyBears.class, HillGiant.class, SliverConstruct.class, GiantGrowth.class})
class CyclopsTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Cyclops Tyrant can block an attacker with power 3")
    void canBlockPowerThreeAttacker() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CyclopsTyrant());
        tyrant.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        prepareBlockers(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(tyrant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cyclops Tyrant cannot block an attacker with power 2")
    void cannotBlockPowerTwoAttacker() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CyclopsTyrant());
        tyrant.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with power 3 or greater");
    }

    @Test
    void intimidateRejectsNonartifactBlockerWithoutSharedColor() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CyclopsTyrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void intimidateAllowsRedBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CyclopsTyrant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareBlockers(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateAllowsColorlessArtifactCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CyclopsTyrant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SliverConstruct());
        prepareBlockers(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void blockingRestrictionUsesBoostedPower() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CyclopsTyrant());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        prepareBlockers(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(tyrant.isBlocking()).isTrue();
    }

    private void prepareBlockers(Permanent attacker) {
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
