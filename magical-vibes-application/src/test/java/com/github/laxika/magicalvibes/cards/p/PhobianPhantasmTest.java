package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhobianPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Phobian Phantasm")
    void paysCumulativeUpkeep() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(phantasm.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phantasm);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Phobian Phantasm")
    void declineSacrifices() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new PhobianPhantasm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(phantasm);
        harness.assertInGraveyard(player1, "Phobian Phantasm");
    }

    @Test
    @DisplayName("Fear prevents non-black creatures from blocking Phobian Phantasm")
    void fearPreventsNonBlackBlockers() {
        Permanent phantasm = new Permanent(new PhobianPhantasm());
        phantasm.setSummoningSick(false);
        phantasm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(phantasm);

        Permanent cloudSprite = new Permanent(new CloudSprite());
        cloudSprite.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(cloudSprite);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear allows black creatures to block Phobian Phantasm")
    void fearAllowsBlackBlockers() {
        Permanent phantasm = new Permanent(new PhobianPhantasm());
        phantasm.setSummoningSick(false);
        phantasm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(phantasm);

        Permanent imp = new Permanent(new DuskImp());
        imp.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(imp);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }
}
