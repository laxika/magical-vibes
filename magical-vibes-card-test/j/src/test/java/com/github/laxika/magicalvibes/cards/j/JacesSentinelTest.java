package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RiverSneak;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacesSentinel.class, JaceIngeniousMindMage.class, RiverSneak.class})
class JacesSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when controller controls a Jace planeswalker")
    void getsPowerBoostWithJace() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        harness.addToBattlefield(player1, new JaceIngeniousMindMage());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2); // 1 base + 1 bonus
    }

    @Test
    @DisplayName("Can't be blocked when controller controls a Jace planeswalker")
    void cantBeBlockedWithJace() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        harness.addToBattlefield(player1, new JaceIngeniousMindMage());

        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();
    }

    @Test
    @DisplayName("No power boost without a Jace planeswalker")
    void noPowerBoostWithoutJace() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1); // 1 base, no bonus
    }

    @Test
    @DisplayName("Can be blocked without a Jace planeswalker")
    void canBeBlockedWithoutJace() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());

        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    @Test
    @DisplayName("Non-Jace creature does not grant bonus")
    void nonJaceDoesNotGrantBonus() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        harness.addToBattlefield(player1, new RiverSneak());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    @Test
    @DisplayName("Loses +1/+0 and can't be blocked when Jace leaves the battlefield")
    void losesBonusWhenJaceLeaves() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceIngeniousMindMage());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(jace);

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Jace planeswalker does not grant bonus")
    void opponentJaceDoesNotCount() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        harness.addToBattlefield(player2, new JaceIngeniousMindMage());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    @Test
    @DisplayName("Bonus begins immediately when Jace enters and does not change toughness")
    void gainsBonusWhenJaceEnters() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        int originalToughness = gqs.getEffectiveToughness(gd, sentinel);
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();

        harness.addToBattlefield(player1, new JaceIngeniousMindMage());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(originalToughness);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();
    }

    @Test
    @DisplayName("Jace leaving your control immediately removes the bonus")
    void losesBonusWhenJaceChangesController() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceIngeniousMindMage());
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(jace);
        gd.playerBattlefields.get(player2.getId()).add(jace);

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    @Test
    @DisplayName("A legal blocker cannot block Sentinel while its controller controls Jace")
    void jacePreventsBlocking() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JacesSentinel());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RiverSneak());
        var defenders = gd.playerBattlefields.get(player2.getId());
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, sentinel, defenders)).isTrue();

        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceIngeniousMindMage());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, sentinel, defenders)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(jace);

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, sentinel, defenders)).isTrue();
    }

}
