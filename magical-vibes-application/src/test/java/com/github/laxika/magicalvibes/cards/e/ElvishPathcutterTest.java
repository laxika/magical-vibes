package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
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

@CardUsed({ElvishPathcutter.class, ElvishWarrior.class, GlorySeeker.class, Forest.class})
class ElvishPathcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants forestwalk to target Elf creature")
    void grantsForestwalkToTargetElf() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk wears off at end of turn")
    void forestwalkWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elf, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target Elf creatures")
    void cannotTargetNonElfCreature() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf creature");
    }

    @Test
    @DisplayName("Ability can target an Elf creature an opponent controls")
    void canTargetOpponentsElf() {
        harness.addToBattlefield(player1, new ElvishPathcutter());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, opponentElf.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk prevents blocking while the defending player controls a Forest")
    void forestwalkPreventsBlockingWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        addCreatureReady(player1, new ElvishPathcutter());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elf);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Granted forestwalk allows blocking when the defending player controls no Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        addCreatureReady(player1, new ElvishPathcutter());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elf);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
