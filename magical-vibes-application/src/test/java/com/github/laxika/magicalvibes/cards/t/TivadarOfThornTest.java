package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TivadarOfThorn.class, GoblinPiker.class, GrizzlyBears.class})
class TivadarOfThornTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target Goblin")
    void etbDestroysTargetGoblin() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("Cannot target a non-Goblin")
    void cannotTargetNonGoblin() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    @Test
    @DisplayName("ETB does not trigger without a Goblin target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new TivadarOfThorn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tivadar of Thorn");
    }
}
