package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeckSnap.class, HillcomberGiant.class})
class NeckSnapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target attacking creature")
    void destroysAttacker() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new NeckSnap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Hillcomber Giant");
        harness.assertInGraveyard(player2, "Hillcomber Giant");
    }

    @Test
    @DisplayName("Destroys target blocking creature")
    void destroysBlocker() {
        Permanent blocker = addBlocker(player2);
        harness.setHand(player1, List.of(new NeckSnap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Hillcomber Giant");
        harness.assertInGraveyard(player2, "Hillcomber Giant");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = addCreatureReady(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new NeckSnap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new NeckSnap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Hillcomber Giant");
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("fizzles"));
    }

    @Test
    @DisplayName("Fizzles if the target stops blocking before resolution")
    void fizzlesIfTargetStopsBlockingBeforeResolution() {
        Permanent blocker = addBlocker(player2);
        harness.setHand(player1, List.of(new NeckSnap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, blocker.getId());
        blocker.clearCombatState();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Hillcomber Giant");
        harness.assertNotInGraveyard(player2, "Hillcomber Giant");
        harness.assertInGraveyard(player1, "Neck Snap");
    }

    @Test
    @DisplayName("Can destroy your own attacking creature")
    void destroysOwnAttacker() {
        Permanent attacker = addCreatureReady(player1, new HillcomberGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new NeckSnap()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Hillcomber Giant");
        harness.assertInGraveyard(player1, "Hillcomber Giant");
        harness.assertInGraveyard(player1, "Neck Snap");
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new HillcomberGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new HillcomberGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
