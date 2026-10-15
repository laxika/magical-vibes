package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.v.VoicelessSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlegroundGeist.class, AbbeyGriffin.class, VoicelessSpirit.class})
class BattlegroundGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Battleground Geist does not buff itself")
    void doesNotBuffItself() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new BattlegroundGeist());

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AbbeyGriffin());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Spirit creatures")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        Permanent opponentGeist = harness.addToBattlefieldAndReturn(player2, new BattlegroundGeist());

        // Opponent's Geist should have base 3/3, no buff from player1's Geist
        assertThat(gqs.getEffectivePower(gd, opponentGeist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGeist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Battleground Geists buff each other with +1/+0")
    void twoGeistsBuffEachOther() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.addToBattlefield(player1, new BattlegroundGeist());

        List<Permanent> geists = findPermanents(player1, "Battleground Geist");

        assertThat(geists).hasSize(2);
        for (Permanent geist : geists) {
            // 3 base + 1 from the other Geist = 4 power, toughness stays 3
            assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Battleground Geist leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.addToBattlefield(player1, new BattlegroundGeist());

        List<Permanent> geists = findPermanents(player1, "Battleground Geist");

        // Both should be 4/3 (buffed by the other)
        assertThat(gqs.getEffectivePower(gd, geists.get(0))).isEqualTo(4);

        // Remove one Geist
        gd.playerBattlefields.get(player1.getId()).remove(geists.get(1));

        // Remaining Geist goes back to base 3/3
        assertThat(gqs.getEffectivePower(gd, geists.get(0))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geists.get(0))).isEqualTo(3);
    }

    @Test
    @DisplayName("A Spirit entering after Battleground Geist immediately gets +1/+0")
    void buffsSpiritEnteringLater() {
        harness.addToBattlefield(player1, new BattlegroundGeist());

        Permanent spirit = harness.enterBattlefieldAndReturn(player1, new VoicelessSpirit());

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonuses stack on an existing Spirit and disappear as sources leave")
    void bonusesStackOnOtherSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new VoicelessSpirit());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BattlegroundGeist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BattlegroundGeist());

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }
}
