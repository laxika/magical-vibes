package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AcceleratedMutation;
import com.github.laxika.magicalvibes.cards.d.DragonFangs;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
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
        ClawsOfWirewood.class, TreetopScout.class})
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
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
