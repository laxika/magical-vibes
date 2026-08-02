package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradewindRiderTest extends BaseCardTest {

    private static final int BOUNCE_ABILITY = 0;

    @Test
    @DisplayName("Returns target creature to its owner's hand, tapping the source and two other creatures")
    void bouncesTargetCreature() {
        Permanent rider = addReadyRider(player1);
        Permanent helper1 = addReadyBears(player1);
        Permanent helper2 = addReadyBears(player1);
        prepareMainPhase();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(rider.isTapped()).isTrue();
        assertThat(helper1.isTapped()).isTrue();
        assertThat(helper2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can return a noncreature permanent such as a land")
    void bouncesLand() {
        Permanent rider = addReadyRider(player1);
        addReadyBears(player1);
        addReadyBears(player1);
        prepareMainPhase();

        harness.addToBattlefield(player2, new Forest());
        UUID victim = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate without two other untapped creatures")
    void requiresTwoOtherUntappedCreatures() {
        Permanent rider = addReadyRider(player1);
        addReadyBears(player1); // only one other creature besides the Rider
        prepareMainPhase();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, rider), BOUNCE_ABILITY, null, victim))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyRider(Player player) {
        return addReady(player, new Permanent(new TradewindRider()));
    }

    private Permanent addReadyBears(Player player) {
        return addReady(player, new Permanent(new GrizzlyBears()));
    }

    private Permanent addReady(Player player, Permanent permanent) {
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
