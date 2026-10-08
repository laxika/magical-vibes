package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Zoanthrope.class, GrizzlyBears.class})
class ZoanthropeTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X counters and Warp Blast deals X damage to a player")
    void ravenousAndWarpBlast() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);
        castZoanthrope(5, player2.getId());

        Permanent zoanthrope = findPermanent(player1, "Zoanthrope");
        assertThat(zoanthrope.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castZoanthrope(4, player2.getId());

        assertThat(findPermanent(player1, "Zoanthrope")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Warp Blast can target and destroy a creature")
    void warpBlastDamagesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castZoanthrope(2, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castZoanthrope(int x, java.util.UUID targetId) {
        putZoanthropeOnStack(x, targetId);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Ravenous draw and Warp Blast are separate triggered abilities")
    void ravenousAndWarpBlastTriggerSeparately() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        putZoanthropeOnStack(5, player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Ravenous still draws when Warp Blast's target leaves the battlefield")
    void ravenousDrawSurvivesIllegalWarpBlastTarget() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        putZoanthropeOnStack(5, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Zoanthrope")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting with X=0 dies without drawing or dealing damage")
    void zeroXDiesWithoutDamageOrDraw() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castZoanthrope(0, player2.getId());

        harness.assertInGraveyard(player1, "Zoanthrope");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void putZoanthropeOnStack(int x, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Zoanthrope()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castCreature(player1, 0, x, targetId);
    }
}
