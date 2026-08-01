package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriangleOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices self and both creatures deal damage equal to their power")
    void creaturesFight() {
        Permanent triangle = addTriangle(player1);
        Permanent mine = addCreatureReady(player1, new HillGiant());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(triangle);
        assertThat(mine.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(theirs);
    }

    @Test
    @DisplayName("Second target must be a creature an opponent controls")
    void secondTargetMustBeOpponents() {
        addTriangle(player1);
        Permanent first = addCreatureReady(player1, new HillGiant());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First target must be a creature you control")
    void firstTargetMustBeControlled() {
        addTriangle(player1);
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        Permanent mine = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(theirs.getId(), mine.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if a target leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        addTriangle(player1);
        Permanent mine = addCreatureReady(player1, new HillGiant());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(theirs);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mine.getMarkedDamage()).isZero();
    }

    private Permanent addTriangle(Player player) {
        Permanent perm = new Permanent(new TriangleOfWar());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
