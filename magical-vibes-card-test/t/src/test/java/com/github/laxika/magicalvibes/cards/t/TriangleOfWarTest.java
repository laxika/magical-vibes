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
    @DisplayName("Neither creature deals fight damage if one target leaves before resolution")
    void noFightWhenTargetLeaves() {
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

    @Test
    @DisplayName("Sacrifice is paid immediately and tapped creatures can fight")
    void tappedCreaturesCanFight() {
        Permanent triangle = harness.addToBattlefieldAndReturn(player1, new TriangleOfWar());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new PantherWarriors());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new Warthog());
        triangle.tap();
        mine.tap();
        theirs.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));

        harness.assertInGraveyard(player1, "Triangle of War");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(triangle);
        assertThat(mine.getMarkedDamage()).isZero();
        assertThat(theirs.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Panther Warriors");
        harness.assertInGraveyard(player2, "Warthog");
    }

    @Test
    @DisplayName("No fight occurs if your target becomes controlled by the opponent")
    void noFightWhenFirstTargetChangesController() {
        harness.addToBattlefield(player1, new TriangleOfWar());
        Permanent mine = addCreatureReady(player1, new PantherWarriors());
        Permanent theirs = addCreatureReady(player2, new Warthog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(mine);
        gd.playerBattlefields.get(player2.getId()).add(mine);
        harness.passBothPriorities();

        assertThat(mine.getMarkedDamage()).isZero();
        assertThat(theirs.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mine, theirs);
    }

    @Test
    @DisplayName("No fight occurs if the opponent's target becomes controlled by you")
    void noFightWhenSecondTargetChangesController() {
        harness.addToBattlefield(player1, new TriangleOfWar());
        Permanent mine = addCreatureReady(player1, new PantherWarriors());
        Permanent theirs = addCreatureReady(player2, new Warthog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(mine.getId(), theirs.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(theirs);
        gd.playerBattlefields.get(player1.getId()).add(theirs);
        harness.passBothPriorities();

        assertThat(mine.getMarkedDamage()).isZero();
        assertThat(theirs.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mine, theirs);
    }
}
