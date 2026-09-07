package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingbladeAssassin.class, GrizzlyBears.class})
class StingbladeAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an opponent's creature dealt damage this turn")
    void etbDestroysDamagedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new StingbladeAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new StingbladeAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("ETB cannot target a damaged creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        harness.setHand(player1, List.of(new StingbladeAssassin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}
