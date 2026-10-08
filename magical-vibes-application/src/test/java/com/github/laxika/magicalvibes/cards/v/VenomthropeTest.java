package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PoisonArrow;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Venomthrope.class, ShivanDragon.class, GrizzlyBears.class, PoisonArrow.class})
class VenomthropeTest extends BaseCardTest {

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new Venomthrope());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchDestroysLargerBlocker() {
        Permanent venomthrope = addCreatureReady(player1, new Venomthrope());
        Permanent blocker = addCreatureReady(player2, new ShivanDragon());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(venomthrope::equals);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(blocker::equals);
    }

    @Test
    void hexproofPreventsOpponentTargeting() {
        Permanent venomthrope = addCreatureReady(player1, new Venomthrope());
        harness.setHand(player2, List.of(new PoisonArrow()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, venomthrope.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void hexproofAllowsControllerToTargetVenomthrope() {
        Permanent venomthrope = addCreatureReady(player1, new Venomthrope());
        harness.setHand(player1, List.of(new PoisonArrow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, venomthrope.getId());

        harness.assertNotOnBattlefield(player1, "Venomthrope");
        harness.assertInGraveyard(player1, "Venomthrope");
        harness.assertLife(player1, 23);
    }

    @Test
    void deathtouchDestroysLargerAttackerWhenBlocking() {
        addCreatureReady(player1, new ShivanDragon());
        addCreatureReady(player2, new Venomthrope());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shivan Dragon");
        harness.assertInGraveyard(player2, "Venomthrope");
        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        harness.assertNotOnBattlefield(player2, "Venomthrope");
    }
}
