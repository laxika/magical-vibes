package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhirapurOrrery.class, AngelOfMercy.class, GrizzlyBears.class, Forest.class, WitchbaneOrb.class})
class GhirapurOrreryTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with an empty hand draws three cards during their upkeep")
    void emptyActivePlayerDrawsThreeCards() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The active player draws during an opponent's upkeep")
    void opponentUpkeepDrawsForActivePlayer() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An active player with cards in hand does not draw")
    void nonEmptyActivePlayerDoesNotDraw() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("The empty-hand condition is checked again when the trigger resolves")
    void emptyHandConditionIsCheckedAtResolution() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        gd.playerHands.get(player1.getId()).add(new AngelOfMercy());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Each player may play an additional land each turn")
    void raisesLandPlayLimitForEachPlayer() {
        harness.addToBattlefield(player1, new GhirapurOrrery());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
    }

    @Test
    void nonEmptyHandPreventsTriggerAtBeginningOfUpkeep() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of(new GhirapurOrrery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void multipleOrreriesOnlyDrawThreeWhenHandStaysNonEmptyAfterFirstDraw() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.addToBattlefield(player2, new GhirapurOrrery());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void triggerStillDrawsAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void eachPlayerCanPlayTwoLandsOnTheirOwnTurnButNotThree() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        for (var player : List.of(player1, player2)) {
            harness.forceActivePlayer(player);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player, List.of(new Forest(), new Forest(), new Forest()));

            harness.playLand(player, 0);
            harness.playLand(player, 0);

            assertThatThrownBy(() -> harness.playLand(player, 0))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(countPermanents(player, "Forest")).isEqualTo(2);
        }
    }

    @Test
    void additionalLandPermissionsStackAndDisappearWithTheirSources() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.addToBattlefield(player2, new GhirapurOrrery());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        gd.playerBattlefields.get(player2.getId()).clear();

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof GhirapurOrrery);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    void opponentControlledOrreryDrawsForPlayerWithHexproof() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
