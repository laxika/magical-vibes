package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishBard.class, GrizzlyBears.class, Humility.class, AesthirGlider.class, DuelingGrounds.class})
class ElvishBardTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Elvish Bard")
    void allAbleCreaturesMustBlock() {
        addCreatureReady(player1, new ElvishBard());

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        // Only one blocker assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both blockers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Elvish Bard")
    void tappedCreaturesNotForcedToBlock() {
        addCreatureReady(player1, new ElvishBard());

        Permanent untapped = addCreatureReady(player2, new GrizzlyBears());
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("No blockers are required when the defending player controls no creatures")
    void noBlockersRequiredWhenDefenderControlsNoCreatures() {
        addCreatureReady(player1, new ElvishBard());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void noBlockersAreIllegalWhenAbleCreatureExists() {
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Every able blocker must block Elvish Bard when another attacker is present")
    void everyAbleBlockerMustBlockBardWhenAnotherAttackerIsPresent() {
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    void losingAllAbilitiesRemovesTheBlockRequirement() {
        addCreatureReady(player1, new ElvishBard());
        harness.addToBattlefield(player1, new Humility());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void noBlockRequiredWhenNoCreatureCanBlock() {
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player2, new AesthirGlider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void blockersMaySplitBetweenTwoAttackingBards() {
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    void oneBlockSatisfiesTwoBardsWhenOnlyOneCreatureMayBlock() {
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player1, new ElvishBard());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        // The restriction enters after attackers are declared, so both Bards remain attacking.
        harness.addToBattlefield(player1, new DuelingGrounds());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
