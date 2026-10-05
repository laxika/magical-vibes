package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KhalniHeartExpedition;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheRoil.class, GrizzlyBears.class, AngelicChorus.class, Island.class,
        KrakenHatchling.class, KhalniHeartExpedition.class})
class IntoTheRoilTest extends BaseCardTest {

    @Test
    void returnsNonlandPermanentWithoutKicker() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(handSizeBefore - 1);
    }

    @Test
    void returnsPermanentAndDrawsWhenKicked() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castKickedInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(handSizeBefore);
    }

    @Test
    void returnsOwnNonlandPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new AngelicChorus()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInHand(player1, "Angelic Chorus");
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void fizzlesIfTargetIsRemovedBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Into the Roil");
    }

    @Test
    void kickedSpellDoesNotDrawWhenItsTargetLeavesInResponse() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Kraken Hatchling");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Into the Roil");
    }

    @Test
    void kickedSpellReturnsNoncreaturePermanentAndDrawsExactlyOneCard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KhalniHeartExpedition()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.setLibrary(player1, List.of(new Island(), new KrakenHatchling()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Khalni Heart Expedition");
        harness.assertInHand(player2, "Khalni Heart Expedition");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Into the Roil");
    }

    @Test
    void cannotKickWithoutEnoughBlueManaForBothCosts() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling()).getId();
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Into the Roil");
        harness.assertOnBattlefield(player2, "Kraken Hatchling");
        assertThat(gd.stack).isEmpty();
    }
}
