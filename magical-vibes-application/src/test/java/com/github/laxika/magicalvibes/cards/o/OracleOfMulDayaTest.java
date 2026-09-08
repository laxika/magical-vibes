package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OracleOfMulDayaTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play a land from the top of their library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("The controller gets one additional land play from the top each turn")
    void getsAdditionalLandPlayFromTop() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        gd.playerDecks.get(player1.getId()).addAll(0, List.of(first, second, third));

        harness.castFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND)))
                .hasSize(2);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(third);
    }

    @Test
    @DisplayName("A nonland top card cannot be played through the land permission")
    void cannotPlayNonlandFromLibraryTop() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("The top-library land permission only applies to its controller")
    void onlyControllerCanPlayFromLibraryTop() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Forest forest = new Forest();
        gd.playerDecks.get(player2.getId()).addFirst(forest);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(forest);
    }
}
