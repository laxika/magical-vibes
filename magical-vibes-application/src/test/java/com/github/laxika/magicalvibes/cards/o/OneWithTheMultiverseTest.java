package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneWithTheMultiverseTest extends BaseCardTest {

    private Card targetlessInstant() {
        Card card = new Card();
        card.setName("Targetless Instant");
        card.setType(CardType.INSTANT);
        card.setManaCost("{5}");
        return card;
    }

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
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(opt);
    }

    @Test
    @DisplayName("Casts one spell from the top of the library without paying its mana cost")
    void castsOneSpellFromTopOfLibraryForFree() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        Card instant = targetlessInstant();
        harness.setLibrary(player1, List.of(instant));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Targetless Instant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casts one spell from hand without paying its mana cost during its controller's turn")
    void castsOneSpellFromHandForFree() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        harness.setHand(player1, List.of(targetlessInstant(), targetlessInstant()));

        harness.castInstant(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant the free cast during an opponent's turn")
    void freeCastIsLimitedToControllerTurn() {
        harness.addToBattlefield(player1, new OneWithTheMultiverse());
        Card instant = targetlessInstant();
        harness.setHand(player1, List.of(instant));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
    }
}
