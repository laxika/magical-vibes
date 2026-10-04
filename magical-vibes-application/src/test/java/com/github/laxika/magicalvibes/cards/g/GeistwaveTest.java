package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SilverBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Geistwave.class, CandlegroveWitch.class, Island.class, SilverBolt.class})
class GeistwaveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you control and draws a card")
    void returnsOwnPermanentAndDraws() {
        harness.addToBattlefield(player1, new CandlegroveWitch());
        harness.setLibrary(player1, List.of(new Island()));

        castAt(harness.getPermanentId(player1, "Candlegrove Witch"));

        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        harness.assertInHand(player1, "Candlegrove Witch");
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Geistwave");
    }

    @Test
    @DisplayName("Returns an opponent's permanent without drawing")
    void returnsOpponentsPermanentWithoutDrawing() {
        harness.addToBattlefield(player2, new CandlegroveWitch());
        UUID targetId = harness.getPermanentId(player2, "Candlegrove Witch");

        castAt(targetId);

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        harness.assertInHand(player2, "Candlegrove Witch");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Geistwave()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("A borrowed permanent returns to its owner and its controller draws")
    void returnsBorrowedPermanentToOwnerAndDraws() {
        CandlegroveWitch witch = new CandlegroveWitch();
        witch.setOwnerId(player2.getId());
        var target = harness.addToBattlefieldAndReturn(player1, witch);
        harness.setLibrary(player1, List.of(new Island()));

        castAt(target.getId());

        harness.assertNotOnBattlefield(player1, "Candlegrove Witch");
        harness.assertInHand(player2, "Candlegrove Witch");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Owning an opponent-controlled permanent does not grant a draw")
    void doesNotDrawForOwnedPermanentControlledByOpponent() {
        CandlegroveWitch witch = new CandlegroveWitch();
        witch.setOwnerId(player1.getId());
        var target = harness.addToBattlefieldAndReturn(player2, witch);
        harness.setLibrary(player1, List.of(new Island()));

        castAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        harness.assertInHand(player1, "Candlegrove Witch");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An absent target prevents the conditional draw")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player1, new CandlegroveWitch());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Geistwave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Candlegrove Witch");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Geistwave");
    }

    @Test
    @DisplayName("Can return a noncreature artifact and draw")
    void returnsNoncreaturePermanentAndDraws() {
        harness.addToBattlefield(player1, new SilverBolt());
        harness.setLibrary(player1, List.of(new Island()));

        castAt(harness.getPermanentId(player1, "Silver Bolt"));

        harness.assertNotOnBattlefield(player1, "Silver Bolt");
        harness.assertInHand(player1, "Silver Bolt");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Control is checked at resolution rather than when cast")
    void doesNotDrawWhenControlIsLostBeforeResolution() {
        CandlegroveWitch witch = new CandlegroveWitch();
        witch.setOwnerId(player1.getId());
        var target = harness.addToBattlefieldAndReturn(player1, witch);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new Geistwave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Candlegrove Witch");
        harness.assertInHand(player1, "Candlegrove Witch");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(1);
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new Geistwave()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
