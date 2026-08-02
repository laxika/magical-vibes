package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightOfDay;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SereneOfferingTest extends BaseCardTest {

    private void castSereneOffering(UUID targetId) {
        harness.setHand(player1, List.of(new SereneOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Destroys the target enchantment and controller gains life equal to its mana value")
    void destroysAndGainsLife() {
        harness.setLife(player1, 15);
        harness.addToBattlefield(player2, new LightOfDay()); // {3}{W} -> mana value 4
        UUID targetId = harness.getPermanentId(player2, "Light of Day");

        castSereneOffering(targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Light of Day");
        harness.assertInGraveyard(player2, "Light of Day");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Life gained scales with the destroyed enchantment's mana value")
    void lifeGainScalesWithManaValue() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player2, new CircleOfProtectionRed()); // {1}{W} -> mana value 2
        UUID targetId = harness.getPermanentId(player2, "Circle of Protection: Red");

        castSereneOffering(targetId);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new LightOfDay()); // legal target elsewhere
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new SereneOffering()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    @Test
    @DisplayName("Fizzles with no life gain if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new LightOfDay());
        UUID targetId = harness.getPermanentId(player2, "Light of Day");

        castSereneOffering(targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Serene Offering");
    }
}
