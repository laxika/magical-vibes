package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriangleOfWar.class, PantherWarriors.class, Warthog.class})
class TriangleOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices self and both creatures deal damage equal to their power")
    void creaturesFight() {
        Permanent triangle = harness.addToBattlefieldAndReturn(player1, new TriangleOfWar());
        Permanent mine = addCreatureReady(player1, new PantherWarriors());
        Permanent theirs = addCreatureReady(player2, new Warthog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(triangle);
        assertThat(mine.getMarkedDamage()).isEqualTo(3);
        assertThat(theirs.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(theirs);
    }

    @Test
    @DisplayName("Cannot activate without paying the two-mana cost")
    void requiresTwoMana() {
        Permanent triangle = harness.addToBattlefieldAndReturn(player1, new TriangleOfWar());
        Permanent mine = addCreatureReady(player1, new PantherWarriors());
        Permanent theirs = addCreatureReady(player2, new Warthog());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(triangle);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(theirs);
        assertThat(mine.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Second target must be a creature an opponent controls")
    void secondTargetMustBeOpponents() {
        harness.addToBattlefield(player1, new TriangleOfWar());
        Permanent first = addCreatureReady(player1, new PantherWarriors());
        Permanent second = addCreatureReady(player1, new Warthog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First target must be a creature you control")
    void firstTargetMustBeControlled() {
        harness.addToBattlefield(player1, new TriangleOfWar());
        Permanent theirs = addCreatureReady(player2, new Warthog());
        Permanent mine = addCreatureReady(player1, new PantherWarriors());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(theirs.getId(), mine.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if a target leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player1, new TriangleOfWar());
        Permanent mine = addCreatureReady(player1, new PantherWarriors());
        Permanent theirs = addCreatureReady(player2, new Warthog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(theirs);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mine.getMarkedDamage()).isZero();
    }
}
