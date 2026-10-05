package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LidlessGaze.class, Forest.class, GrizzlyBears.class})
class LidlessGazeTest extends BaseCardTest {

    @Test
    void controllerMayPlayTheTopCardExiledFromEachLibraryWithAnyColorMana() {
        Card ownTopCard = new Forest();
        Card opponentTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setHand(player1, List.of(new LidlessGaze()));
        addLidlessGazeMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.exilePlayPermissions)
                .containsEntry(ownTopCard.getId(), player1.getId())
                .containsEntry(opponentTopCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaType).contains(opponentTopCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player2, opponentTopCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, opponentTopCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentTopCard);
    }

    @Test
    void flashbackAlsoExilesCardsAndExilesTheSpellAfterResolution() {
        Card ownTopCard = new Forest();
        Card opponentTopCard = new GrizzlyBears();
        Card lidlessGaze = new LidlessGaze();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.setGraveyard(player1, List.of(lidlessGaze));
        addLidlessGazeMana();

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.findExiledCard(ownTopCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentTopCard.getId())).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(lidlessGaze);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(lidlessGaze);
    }

    @Test
    void exiledLandsCanBePlayedButStillConsumeTheNormalLandPlay() {
        Card ownLand = new Forest();
        Card opponentLand = new Forest();
        harness.setLibrary(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(opponentLand));
        harness.setHand(player1, List.of(new LidlessGaze()));
        addLidlessGazeMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castFromExile(player1, opponentLand.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == opponentLand);
        assertThat(gd.findExiledCard(opponentLand.getId())).isNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, ownLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(ownLand.getId())).isNotNull();
    }

    @Test
    void playAndAnyManaPermissionsLastThroughTheControllersNextTurnThenExpire() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownCard, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(opponentCard, new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LidlessGaze()));
        addLidlessGazeMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.findExiledCard(ownCard.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, ownCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, ownCard.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == ownCard);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, opponentCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(opponentCard.getId())).isNotNull();
    }

    @Test
    void emptyLibraryDoesNotPreventExilingTheOtherPlayersTopCard() {
        Card topCard = new GrizzlyBears();
        Card nextCard = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new LidlessGaze()));
        addLidlessGazeMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == topCard);
    }

    @Test
    void castingAnExiledCardStillRequiresPayingItsManaCost() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new LidlessGaze()));
        addLidlessGazeMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == topCard);
    }

    private void addLidlessGazeMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
