package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.w.WoollySpider;
import com.github.laxika.magicalvibes.cards.y.YavimayaGnats;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverErne.class, BalduvianBears.class, WoollySpider.class, YavimayaGnats.class})
class SilverErneTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByGroundCreature() {
        attackingErne();
        Permanent groundBlocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
        assertThat(groundBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Can be blocked by a flying creature")
    void canBeBlockedByFlyer() {
        attackingErne();
        Permanent flyingBlocker = addCreatureReady(player2, new YavimayaGnats());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        assertThat(flyingBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        attackingErne();
        Permanent reachBlocker = addCreatureReady(player2, new WoollySpider());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        assertThat(reachBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToDefender() {
        harness.setLife(player2, 20);
        attackingErne();
        Permanent blocker = addCreatureReady(player2, new YavimayaGnats());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        // 2/2 trample blocked by 0/1 flyer → 1 lethal to blocker, 1 excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Yavimaya Gnats");
    }

    private Permanent attackingErne() {
        Permanent erne = new Permanent(new SilverErne());
        erne.setSummoningSick(false);
        erne.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(erne);
        return erne;
    }
}
