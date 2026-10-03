package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoomerangBasics.class, GrizzlyBears.class, Island.class, BendersWaterskin.class})
class BoomerangBasicsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you control and draws a card")
    void returnsOwnPermanentAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));

        castAt(harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Boomerang Basics");
    }

    @Test
    @DisplayName("Returns an opponent's permanent without drawing")
    void returnsOpponentsPermanentWithoutDrawing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAt(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Can return a noncreature artifact and draw")
    void returnsNoncreatureArtifactAndDraws() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new BendersWaterskin());
        harness.setLibrary(player1, List.of(new Island()));

        castAt(permanent.getId());

        harness.assertNotOnBattlefield(player1, "Bender's Waterskin");
        harness.assertInHand(player1, "Bender's Waterskin");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner but draws for its controller")
    void returnsStolenPermanentToOwnerAndDrawsForCaster() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(permanent.getId(), player2.getId());
        harness.setLibrary(player1, List.of(new Island()));

        castAt(permanent.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Owning an opponent-controlled permanent does not cause a draw")
    void doesNotDrawForOwnedPermanentControlledByOpponent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(permanent.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Island()));

        castAt(permanent.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when the target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, permanent.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, permanent));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Boomerang Basics");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing control before resolution prevents the draw")
    void checksControlAtResolutionAfterLosingControl() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, permanent.getId());
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        gd.stolenCreatures.put(permanent.getId(), player1.getId());

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gaining control before resolution causes the draw")
    void checksControlAtResolutionAfterGainingControl() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, permanent.getId());
        gd.playerBattlefields.get(player2.getId()).remove(permanent);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        gd.stolenCreatures.put(permanent.getId(), player2.getId());

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
