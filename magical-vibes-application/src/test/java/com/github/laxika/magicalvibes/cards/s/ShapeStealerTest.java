package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShapeStealerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature changes Shape Stealer's base power and toughness")
    void blockingCreatureChangesBasePowerAndToughness() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent shapeStealer = addReady(player2, new ShapeStealer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(2);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Becoming blocked changes Shape Stealer's base power and toughness")
    void becomingBlockedChangesBasePowerAndToughness() {
        Permanent shapeStealer = addReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(2);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Shape Stealer's changed base power and toughness last until end of turn")
    void changedBasePowerAndToughnessLastUntilEndOfTurn() {
        Permanent shapeStealer = addReady(player1, new ShapeStealer());
        shapeStealer.setAttacking(true);
        addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(shapeStealer.getEffectivePower()).isEqualTo(2);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapeStealer.getEffectivePower()).isEqualTo(1);
        assertThat(shapeStealer.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
