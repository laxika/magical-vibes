package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Smite.class, SkyshroudFalcon.class, Shock.class})
class SmiteTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a blocked attacker")
    void destroysBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new SkyshroudFalcon());
        addCreatureReady(player2, new SkyshroudFalcon());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castSmite(attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud Falcon");
        harness.assertOnBattlefield(player2, "Skyshroud Falcon");
    }

    @Test
    @DisplayName("Cannot target an unblocked creature")
    void cannotTargetUnblockedCreature() {
        Permanent attacker = addCreatureReady(player1, new SkyshroudFalcon());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThatThrownBy(() -> castSmite(attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the creature doing the blocking")
    void cannotTargetBlocker() {
        Permanent attacker = addCreatureReady(player1, new SkyshroudFalcon());
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> castSmite(blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a blocked creature after its blocker leaves the battlefield")
    void canTargetBlockedCreatureAfterBlockerLeaves() {
        Permanent attacker = addCreatureReady(player1, new SkyshroudFalcon());
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new Smite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud Falcon");
    }

    private void castSmite(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Smite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
    }

}
