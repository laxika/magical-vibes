package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({PartWater.class, DurkwoodBoars.class, Island.class})
class PartWaterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives islandwalk to each of exactly X target creatures")
    void givesIslandwalkToEachTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new PartWater()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, untargeted, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Requires exactly X creature targets")
    void requiresExactlyXTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new PartWater()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Islandwalk wears off at end of turn")
    void islandwalkWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.setHand(player1, List.of(new PartWater()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 1, List.of(bear.getId()));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.ISLANDWALK)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.hasKeyword(gd, bear, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new PartWater()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows zero targets when X is zero")
    void allowsZeroTargetsWhenXIsZero() {
        harness.setHand(player1, List.of(new PartWater()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Part Water");
    }

    @Test
    @DisplayName("Islandwalk prevents blocking while the defender controls an Island")
    void islandwalkPreventsBlockingWhenDefenderControlsIsland() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBoars());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        harness.addToBattlefieldAndReturn(player2, new Island());
        grantIslandwalk(attacker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(player1, List.of(attackerIndex));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Islandwalk allows blocking while the defender controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBoars());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBoars());
        grantIslandwalk(attacker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(player1, List.of(attackerIndex));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void grantIslandwalk(Permanent target) {
        harness.setHand(player1, List.of(new PartWater()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
