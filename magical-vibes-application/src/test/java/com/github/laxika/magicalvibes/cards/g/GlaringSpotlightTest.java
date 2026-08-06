package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlaringSpotlightTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller can target an opponent's hexproof creature")
    void controllerCanTargetOpponentHexproofCreature() {
        harness.addToBattlefield(player1, new GlaringSpotlight());
        Permanent tyrant = addTyrant(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, tyrant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without the Spotlight the same hexproof creature can't be targeted")
    void hexproofStillBlocksWithoutSpotlight() {
        Permanent tyrant = addTyrant(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tyrant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("The Spotlight does not let its controller's opponent target hexproof creatures")
    void doesNotHelpTheOpponent() {
        harness.addToBattlefield(player1, new GlaringSpotlight());
        Permanent tyrant = addTyrant(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, tyrant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Sacrificing it gives your creatures hexproof and makes them unblockable")
    void sacrificeGrantsHexproofAndUnblockable() {
        harness.addToBattlefield(player1, new GlaringSpotlight());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glaring Spotlight");
        assertThat(bears.isCantBeBlocked()).isTrue();
        assertThat(opponentBears.isCantBeBlocked()).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private Permanent addTyrant(Player player) {
        Permanent perm = new Permanent(new CarnageTyrant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
