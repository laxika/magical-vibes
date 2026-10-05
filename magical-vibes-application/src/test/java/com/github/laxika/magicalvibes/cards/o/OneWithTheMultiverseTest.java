package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FogOfWar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneWithTheMultiverse.class, Forest.class, FogOfWar.class})
class OneWithTheMultiverseTest extends BaseCardTest {

    @Test
    @DisplayName("Plays a land from the top of the library")
    void playsLandFromTopOfLibrary() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Casts a spell from the top of the library for its normal cost")
    void castsSpellFromTopOfLibraryForNormalCost() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        FogOfWar instant = new FogOfWar();
        harness.setLibrary(player1, List.of(instant));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Fog of War");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(instant);
    }

    @Test
    @DisplayName("Casts one spell from the top of the library without paying its mana cost")
    void castsOneSpellFromTopOfLibraryForFree() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        Card instant = new FogOfWar();
        harness.setLibrary(player1, List.of(instant));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Fog of War");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casts one spell from hand without paying its mana cost during its controller's turn")
    void castsOneSpellFromHandForFree() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.setHand(player1, List.of(new FogOfWar(), new FogOfWar()));

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant the free cast during an opponent's turn")
    void freeCastIsLimitedToControllerTurn() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        Card instant = new FogOfWar();
        harness.setHand(player1, List.of(instant));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
    }

    @Test
    void handAndLibraryShareTheFreeCastAllowance() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.setHand(player1, List.of(new FogOfWar()));
        FogOfWar top = new FogOfWar();
        harness.setLibrary(player1, List.of(top));

        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveFromLibraryTop(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void eachCopyGrantsAnIndependentFreeCast() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.setHand(player1, List.of(new FogOfWar(), new FogOfWar(), new FogOfWar()));

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void playingLandDoesNotSpendTheFreeCastOrGrantExtraLandPlays() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), secondLand));
        harness.setHand(player1, List.of(new FogOfWar()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand);
        harness.castAndResolveInstant(player1, 0);
        harness.assertInGraveyard(player1, "Fog of War");
    }

    @Test
    void topCardIsVisibleOnlyToControllerEvenOnOpponentsTurn() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.setLibrary(player1, List.of(new FogOfWar()));
        harness.forceActivePlayer(player2);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Fog of War"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Fog of War"));
    }

    @Test
    void libraryPermissionDoesNotGiveEnchantmentsFlash() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        OneWithTheMultiverse top = new OneWithTheMultiverse();
        harness.setLibrary(player1, List.of(top));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 8);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
