package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpenTheWay.class, Forest.class, GrizzlyBears.class, Mountain.class})
class OpenTheWayTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals until X lands, puts them onto the battlefield tapped, and bottoms the rest")
    void revealsUntilXLandCards() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(forest, bears, mountain));
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast with X greater than the number of players")
    void xCannotExceedNumberOfPlayers() {
        harness.setHand(player1, List.of(new OpenTheWay()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X can't be greater than 2");
    }
}
