package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
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

@CardUsed({UnseenWalker.class, Forest.class, ViashinoWarrior.class})
class UnseenWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants forestwalk to target creature")
    void grantsForestwalkToTarget() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk prevents blocking when the defending player controls a Forest")
    void grantedForestwalkPreventsBlocking() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent attacker = addCreatureReady(player1, new ViashinoWarrior());
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted forestwalk wears off at end of turn")
    void forestwalkWearsOff() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Printed forestwalk prevents blocking when the defending player controls a Forest")
    void printedForestwalkPreventsBlocking() {
        Permanent walker = addCreatureReady(player1, new UnseenWalker());
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new ViashinoWarrior());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(walker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(walker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Printed forestwalk does not prevent blocking when the defending player controls no Forest")
    void printedForestwalkAllowsBlockingWithoutForest() {
        Permanent walker = addCreatureReady(player1, new UnseenWalker());
        Permanent blocker = addCreatureReady(player2, new ViashinoWarrior());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(walker)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(walker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot be activated without all of its mana cost")
    void requiresFullManaCost() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability requires two green mana even when three mana are available")
    void requiresTwoGreenMana() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Walker can grant forestwalk repeatedly")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new UnseenWalker());
        walker.tap();
        walker.setSummoningSick(true);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ViashinoWarrior());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FORESTWALK)).isTrue();
        assertThat(walker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk does not prevent blocking when only the attacker controls a Forest")
    void attackersForestDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new UnseenWalker());
        Permanent attacker = addCreatureReady(player1, new ViashinoWarrior());
        harness.addToBattlefield(player1, new Forest());
        Permanent blocker = addCreatureReady(player2, new ViashinoWarrior());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
