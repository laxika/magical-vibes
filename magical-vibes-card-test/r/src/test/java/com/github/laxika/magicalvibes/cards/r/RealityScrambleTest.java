package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealityScramble.class, Forest.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class RealityScrambleTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an owned permanent on the library bottom and replaces it with a shared-type permanent")
    void replacesOwnedPermanentWithSharedTypePermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant(), new Mountain()));
        harness.setHand(player1, List.of(new RealityScramble()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Mountain", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Reality Scramble");
    }

    @Test
    @DisplayName("Uses your library when you own the targeted permanent controlled by an opponent")
    void usesOwnersLibraryForStolenPermanent() {
        Card ownedBears = new GrizzlyBears();
        ownedBears.setOwnerId(player1.getId());
        Permanent stolenBears = harness.addToBattlefieldAndReturn(player2, ownedBears);
        gd.stolenCreatures.put(stolenBears.getId(), player1.getId());
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));
        harness.setHand(player1, List.of(new RealityScramble()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, stolenBears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetPermanentOpponentOwns() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RealityScramble()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("Retrace recasts Reality Scramble by discarding a land")
    void retraceDiscardsLandAndResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new HillGiant()));
        harness.setGraveyard(player1, List.of(new RealityScramble()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castRetrace(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Reality Scramble", "Mountain", "Grizzly Bears");
    }
}
