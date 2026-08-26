package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShockerUnshakable.class, GrizzlyBears.class})
class ShockerUnshakableTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to target creature and its controller")
    void etbDealsDamageToCreatureAndController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new ShockerUnshakable()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllerTurn() {
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new ShockerUnshakable());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during an opponent's turn")
    void doesNotHaveFirstStrikeDuringOpponentsTurn() {
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new ShockerUnshakable());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, shocker, Keyword.FIRST_STRIKE)).isFalse();
    }
}
