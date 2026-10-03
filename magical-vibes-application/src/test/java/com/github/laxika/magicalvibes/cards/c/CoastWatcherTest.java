package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AcceleratedMutation;
import com.github.laxika.magicalvibes.cards.d.DragonFangs;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.cards.x.XantidSwarm;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastWatcher.class, AcceleratedMutation.class, DragonFangs.class,
        ClawsOfWirewood.class, TreetopScout.class, XantidSwarm.class})
class CoastWatcherTest extends BaseCardTest {

    @Test
    void hasProtectionFromGreen() {
        Permanent coastWatcher = addCreatureReady(player1, new CoastWatcher());

        assertThat(gqs.hasProtectionFrom(gd, coastWatcher, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, coastWatcher, CardColor.RED)).isFalse();
    }

    @Test
    void cannotBeTargetedByGreenInstant() {
        Permanent coastWatcher = addCreatureReady(player2, new CoastWatcher());
        addCreatureReady(player2, new TreetopScout());
        harness.setHand(player1, List.of(new AcceleratedMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, coastWatcher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    void cannotBeEnchantedByGreenAura() {
        Permanent coastWatcher = addCreatureReady(player2, new CoastWatcher());
        addCreatureReady(player2, new TreetopScout());
        harness.setHand(player1, List.of(new DragonFangs()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, coastWatcher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    void preventsDamageFromGreenSpell() {
        Permanent coastWatcher = addCreatureReady(player2, new CoastWatcher());
        harness.setHand(player1, List.of(new ClawsOfWirewood()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(coastWatcher.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Coast Watcher");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void cannotBeBlockedByGreenFlyer() {
        addCreatureReady(player1, new CoastWatcher());
        addCreatureReady(player2, new XantidSwarm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new CoastWatcher());
        addCreatureReady(player2, new TreetopScout());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void canBeBlockedAndDamagedByBlueFlyer() {
        addCreatureReady(player1, new CoastWatcher());
        addCreatureReady(player2, new CoastWatcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Coast Watcher");
        harness.assertInGraveyard(player2, "Coast Watcher");
    }

    @Test
    void canBlockGreenCreatureAndPreventItsCombatDamage() {
        addCreatureReady(player1, new TreetopScout());
        Permanent coastWatcher = addCreatureReady(player2, new CoastWatcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Treetop Scout");
        harness.assertOnBattlefield(player2, "Coast Watcher");
        assertThat(coastWatcher.getMarkedDamage()).isZero();
    }
}
