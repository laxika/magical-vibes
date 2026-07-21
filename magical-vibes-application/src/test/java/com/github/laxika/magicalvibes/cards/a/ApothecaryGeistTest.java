package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Apothecary Geist"));
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

    private void castApothecaryGeist() {
        harness.setHand(player1, List.of(new ApothecaryGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
