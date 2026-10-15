package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OracleOfMulDaya.class, Forest.class, GrizzlyBears.class})
class OracleOfMulDayaTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play a land from the top of their library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

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
        harness.setLibrary(player1, List.of(first, second, third));

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
        harness.setLibrary(player1, List.of(bears));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("The top-library land permission only applies to its controller")
    void onlyControllerCanPlayFromLibraryTop() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(forest);
    }

    @Test
    void revealsControllersTopCardToBothPlayersAndUpdatesAfterLandPlay() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        harness.setLibrary(player1, List.of(new Forest(), new OracleOfMulDaya()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.clearMessages();
        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[{")
                            && message.contains("\"name\":\"Forest\"")
                            && message.contains("}],[]]"));
        }

        harness.castFromLibraryTop(player1);
        harness.clearMessages();
        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[{")
                            && message.contains("Oracle of Mul Daya")
                            && message.contains("}],[]]"));
        }
    }

    @Test
    void additionalLandCanBePlayedFromHandAndSharesAllowanceWithLibrary() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void multipleOraclesEachGrantAnAdditionalLandPlay() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(3);
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void losingAbilitiesRemovesAdditionalLandAllowance() {
        var oracle = harness.addToBattlefieldAndReturn(player1, new OracleOfMulDaya());
        harness.setHand(player1, List.of(new TurnToFrog(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, oracle.getId());
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void leavingBattlefieldRemovesRevealAndBothLandPermissions() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof OracleOfMulDaya);
        harness.clearMessages();

        harness.publishState();

        for (var connection : List.of(harness.getConn1(), harness.getConn2())) {
            assertThat(connection.getSentMessages()).anyMatch(message ->
                    message.contains("\"revealedLibraryTopCards\":[[],[]]"));
        }
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void landPermissionDoesNotOverrideNormalTiming() {
        harness.addToBattlefield(player1, new OracleOfMulDaya());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
