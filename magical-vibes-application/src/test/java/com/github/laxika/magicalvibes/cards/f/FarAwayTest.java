package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Far // Away is one card whose two halves (and their fusion) are the three modes of a single
 * modal spell, each paying its own total cost.
 */
class FarAwayTest extends BaseCardTest {

    private static final int FAR = 0;
    private static final int AWAY = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Far returns the targeted creature to its owner's hand")
    void farBouncesTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, FAR, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Far cannot target a player")
    void farCannotTargetPlayer() {
        harness.setHand(player1, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, FAR, playerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Away makes the targeted player sacrifice a creature of their choice")
    void awayMakesTargetPlayerSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, AWAY, player2.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, spider.getId());

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Away is castable off black mana alone — the mode's {2}{B} replaces the printed {1}{U}")
    void awayIsPaidWithItsOwnCost() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, AWAY, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fuse resolves both halves, each against its own target")
    void fuseResolvesBothHalves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, FUSE, List.of(bears.getId(), player2.getId()));
        harness.passBothPriorities();

        // Far bounces the Bears, then Away edicts the only creature player2 has left.
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = bears.getId();
        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(bearsId, playerId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
