package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Shinestriker.class, GrizzlyBears.class, AirElemental.class, HillGiant.class, Forest.class, Unsummon.class})
class ShinestrikerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card when it is the only colored permanent you control")
    void drawsOneForOneColor() {
        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws one card for each distinct color among permanents you control")
    void drawsForDistinctColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new AirElemental(), new HillGiant()));

        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only permanents controlled by its controller")
    void ignoresOpponentColors() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple permanents of the same color count only once")
    void countsDuplicateColorsOnce() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());

        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Forest contributes no color despite producing green mana")
    void ignoresColorlessPermanents() {
        harness.addToBattlefield(player1, new Forest());

        castShinestriker();

        assertThat(drawnCards(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts colors when the enter trigger resolves")
    void countsColorsAtResolution() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new AirElemental(), new HillGiant()));
        harness.castFromHand(player1, new Shinestriker(), "{4}{U}{U}");
        harness.passBothPriorities();
        assertThat(drawnCards(player1)).isZero();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(drawnCards(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws no cards if no colored permanents remain when the trigger resolves")
    void drawsZeroAfterSourceLeaves() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new AirElemental(), new HillGiant()));
        harness.castFromHand(player1, new Shinestriker(), "{4}{U}{U}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Shinestriker"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shinestriker");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private void castShinestriker() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new AirElemental(), new HillGiant()));
        harness.castFromHand(player1, new Shinestriker(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int drawnCards(Player player) {
        return gd.playerHands.get(player.getId()).size();
    }
}
