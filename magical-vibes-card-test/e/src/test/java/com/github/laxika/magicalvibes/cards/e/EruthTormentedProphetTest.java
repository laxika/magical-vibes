package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SharedFate;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.w.WretchedThrong;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EruthTormentedProphet.class, Forest.class, Mountain.class, DressDown.class,
        WretchedThrong.class, SharedFate.class})
class EruthTormentedProphetTest extends BaseCardTest {

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    @Test
    @DisplayName("Replaces a draw by exiling the top two cards with play permission")
    void replacesDrawWithTopTwoExileAndPlayPermission() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveDraw();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Exiles the available card when the library has fewer than two cards")
    void replacesDrawWithShortLibrary() {
        Forest only = new Forest();
        harness.setLibrary(player1, List.of(only));
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        resolveDraw();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not lose the game when replacing a draw from an empty library")
    void replacesEmptyLibraryDrawWithoutLoss() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        resolveDraw();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void replacesEachDrawInAMultipleCardDraw() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        Forest third = new Forest();
        Mountain fourth = new Mountain();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, remaining));
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void doesNotReplaceOpponentsDraw() {
        Forest top = new Forest();
        harness.setLibrary(player2, List.of(top));
        harness.addToBattlefield(player1, new EruthTormentedProphet());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).contains(top);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({DressDown.class})
    void doesNotReplaceDrawAfterLosingAbilities() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EruthTormentedProphet());
        harness.addToBattlefield(player2, new DressDown());

        resolveDraw();

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void canPlayExiledLandButCannotPlayAnAdditionalLand() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EruthTormentedProphet());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        resolveDraw();

        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({WretchedThrong.class})
    void exiledSpellRequiresItsNormalManaCost() {
        WretchedThrong spell = new WretchedThrong();
        harness.setLibrary(player1, List.of(spell));
        harness.addToBattlefield(player1, new EruthTormentedProphet());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        resolveDraw();

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wretched Throng");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void playPermissionExpiresWhenTheTurnEnds() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new EruthTormentedProphet());
        resolveDraw();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
    }

    @Test
    @CardUsed({SharedFate.class})
    void affectedPlayerChoosesBetweenCompetingDrawReplacements() {
        Forest ownFirst = new Forest();
        Mountain ownSecond = new Mountain();
        Forest opponentsTop = new Forest();
        harness.setLibrary(player1, List.of(ownFirst, ownSecond));
        harness.setLibrary(player2, List.of(opponentsTop));
        harness.addToBattlefield(player1, new EruthTormentedProphet());
        harness.addToBattlefield(player2, new SharedFate());

        resolveDraw();

        assertThat(gd.interaction.isAwaitingInput() || !gd.pendingMayAbilities.isEmpty()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownFirst, ownSecond);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTop);
    }
}
