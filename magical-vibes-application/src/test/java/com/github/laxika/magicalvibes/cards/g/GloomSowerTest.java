package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloomSower.class, AlpineWatchdog.class, VillageRites.class})
class GloomSowerTest extends BaseCardTest {

    @Test
    @DisplayName("When Gloom Sower becomes blocked, the blocker controller loses 2 life and Gloom Sower's controller gains 2 life")
    void becomesBlockedDrainsBlockerController() {
        addAttackingGloomSower(player1, player2);
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Gloom Sower triggers once for each creature blocking it")
    void becomesBlockedTriggersOncePerBlocker() {
        addAttackingGloomSower(player1, player2);
        addCreatureReady(player2, new AlpineWatchdog());
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An unblocked Gloom Sower does not trigger its life exchange")
    void unblockedDoesNotExchangeLife() {
        addAttackingGloomSower(player1, player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The life exchange still resolves after the blocker is sacrificed")
    void sacrificedBlockerStillDrainsItsController() {
        addAttackingGloomSower(player1, player2);
        Permanent blocker = addCreatureReady(player2, new AlpineWatchdog());
        harness.setHand(player2, List.of(new VillageRites()));
        harness.setLibrary(player2, List.of(new AlpineWatchdog(), new AlpineWatchdog()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.castInstantWithSacrifice(player2, 0, null, blocker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Alpine Watchdog");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The life exchange still resolves after Gloom Sower is sacrificed")
    void sacrificedSourceStillExchangesLife() {
        Permanent sower = addAttackingGloomSower(player1, player2);
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new VillageRites()));
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstantWithSacrifice(player1, 0, null, sower.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gloom Sower");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private Permanent addAttackingGloomSower(Player attacker, Player defender) {
        Permanent perm = addCreatureReady(attacker, new GloomSower());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
