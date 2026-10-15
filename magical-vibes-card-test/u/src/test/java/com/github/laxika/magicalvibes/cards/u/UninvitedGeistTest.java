package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.s.SanitariumSkeleton;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UninvitedGeist.class, UnimpededTrespasser.class, DevilthornFox.class, SanitariumSkeleton.class})
class UninvitedGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms after dealing combat damage to a player")
    void transformsAfterCombatDamageToPlayer() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard()).isInstanceOf(UnimpededTrespasser.class);
    }

    @Test
    @DisplayName("An equal-power creature can block and prevents damage to the player")
    void doesNotTransformWhenBlocked() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UninvitedGeist());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(geist))));
        harness.passBothPriorities();

        assertThat(geist.isTransformed()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The transformed creature cannot be blocked")
    void transformedCreatureCannotBeBlocked() {
        Permanent trespasser = addCreatureReady(player1, new UnimpededTrespasser());
        trespasser.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UninvitedGeist());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(trespasser)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Skulk prohibits a creature with greater power from blocking")
    void greaterPowerCreatureCannotBlock() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DevilthornFox());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(geist)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("A creature with less power can block without triggering transformation")
    void lesserPowerCreatureCanBlock() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SanitariumSkeleton());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(geist))));
        harness.passBothPriorities();

        assertThat(geist.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(geist);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Later combat damage does not transform the back face back to the front")
    void backFaceRemainsTransformedAfterCombatDamage() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        assertThat(geist.isTransformed()).isTrue();

        geist.setAttacking(true);
        prepareDeclareBlockers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard()).isInstanceOf(UnimpededTrespasser.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
