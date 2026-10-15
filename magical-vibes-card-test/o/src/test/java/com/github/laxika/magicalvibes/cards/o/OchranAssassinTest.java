package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OchranAssassin.class, VernadiShieldmate.class})
class OchranAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Ochran Assassin")
    void allAbleCreaturesMustBlock() {
        Permanent assassin = addCreatureReady(player1, new OchranAssassin());
        assassin.setAttacking(true);

        addCreatureReady(player2, new VernadiShieldmate());
        addCreatureReady(player2, new VernadiShieldmate());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
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
    @DisplayName("Tapped creatures are not forced to block Ochran Assassin")
    void tappedCreaturesNotForcedToBlock() {
        Permanent assassin = addCreatureReady(player1, new OchranAssassin());
        assassin.setAttacking(true);

        Permanent untapped = addCreatureReady(player2, new VernadiShieldmate());
        Permanent tapped = addCreatureReady(player2, new VernadiShieldmate());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("No blockers are required when no creature can block Ochran Assassin")
    void noBlockersWhenDefenderHasNoCreatures() {
        Permanent assassin = addCreatureReady(player1, new OchranAssassin());
        assassin.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker with more toughness than the Assassin's power")
    void deathtouchDestroysBlocker() {
        addCreatureReady(player1, new OchranAssassin());
        addCreatureReady(player2, new VernadiShieldmate());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Ochran Assassin");
        harness.assertInGraveyard(player2, "Vernadi Shieldmate");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each blocker may choose either of two attacking Assassins")
    void competingAssassinsAllowSplitBlocks() {
        addCreatureReady(player1, new OchranAssassin()).setAttacking(true);
        addCreatureReady(player1, new OchranAssassin()).setAttacking(true);
        Permanent first = addCreatureReady(player2, new VernadiShieldmate());
        Permanent second = addCreatureReady(player2, new VernadiShieldmate());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Able blockers cannot block another attacker instead of Ochran Assassin")
    void otherAttackerCannotDivertBlockers() {
        addCreatureReady(player1, new OchranAssassin()).setAttacking(true);
        addCreatureReady(player1, new VernadiShieldmate()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VernadiShieldmate());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }
}
