package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApothecaryGeist.class, ChapelGeist.class, GrizzlyBears.class})
class ApothecaryGeistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 3 life when you control another Spirit")
    void etbGainsLifeWithAnotherSpirit() {
        harness.addToBattlefield(player1, new ChapelGeist());
        castApothecaryGeist();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB triggers when you control another Spirit")
    void etbTriggersWithAnotherSpirit() {
        harness.addToBattlefield(player1, new ChapelGeist());
        castApothecaryGeist();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Apothecary Geist");
    }

    @Test
    @DisplayName("ETB does NOT trigger without another Spirit")
    void etbDoesNotTriggerWithoutAnotherSpirit() {
        castApothecaryGeist();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Apothecary Geist");
    }

    @Test
    @DisplayName("ETB does NOT trigger when only a non-Spirit is controlled")
    void etbDoesNotTriggerWithNonSpirit() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castApothecaryGeist();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does NOT trigger when opponent controls a Spirit")
    void etbDoesNotTriggerWithOpponentSpirit() {
        harness.addToBattlefield(player2, new ChapelGeist());
        castApothecaryGeist();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does nothing if the other Spirit is removed before resolution")
    void etbFizzlesWhenAnotherSpiritRemoved() {
        harness.addToBattlefield(player1, new ChapelGeist());
        castApothecaryGeist();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Chapel Geist"));

        harness.passBothPriorities(); // resolve ETB trigger — condition no longer met

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("controls another matching permanent ability does nothing"));
    }

    @Test
    @DisplayName("ETB still gains life when its source leaves but another Spirit remains")
    void gainsLifeAfterSourceLeaves() {
        harness.addToBattlefield(player1, new ApothecaryGeist());
        var otherSpiritId = harness.getPermanentId(player1, "Apothecary Geist");
        castApothecaryGeist();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> !p.getId().equals(otherSpiritId));
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB gains life if a different Spirit satisfies the condition at resolution")
    void gainsLifeWithReplacementSpirit() {
        harness.addToBattlefield(player1, new ApothecaryGeist());
        var otherSpiritId = harness.getPermanentId(player1, "Apothecary Geist");
        castApothecaryGeist();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(otherSpiritId));
        harness.addToBattlefield(player1, new ApothecaryGeist());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Multiple other Spirits still result in only 3 life gained")
    void gainsLifeOnlyOnceWithMultipleSpirits() {
        harness.addToBattlefield(player1, new ApothecaryGeist());
        harness.addToBattlefield(player1, new ApothecaryGeist());
        castApothecaryGeist();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    private void castApothecaryGeist() {
        harness.castFromHand(player1, new ApothecaryGeist(), "{3}{W}");
    }
}
