package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HugsGrislyGuardian.class, Shock.class, Forest.class})
class HugsGrislyGuardianTest extends BaseCardTest {

    @Test
    void entersAndExilesTopXCardsUntilEndOfNextTurn() {
        Card first = new Shock();
        Card second = new Forest();
        Card third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        castHugs(2);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsAwaitNextTurnOfPlayer).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void exiledCardsCanBePlayedForTheirNormalCosts() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        castHugs(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void controllerMayPlayAnAdditionalLandEachTurn() {
        harness.addToBattlefield(player1, new HugsGrislyGuardian());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard() instanceof Forest)
                .hasSize(2);
    }

    @Test
    void zeroXLeavesLibraryUntouched() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castHugs(0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastExilesNoCards() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player1, new HugsGrislyGuardian());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exilesOnlyAvailableCardsWhenXExceedsLibrarySize() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castHugs(3);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exiledLandsUseTheAdditionalLandAllowance() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        castHugs(3);

        harness.castFromExile(player1, first.getId());
        harness.castFromExile(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard() instanceof Forest)
                .hasSize(2);
        assertThatThrownBy(() -> harness.castFromExile(player1, third.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(third);
    }

    @Test
    void exiledSpellsStillRequireMana() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        castHugs(1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void permissionExpiresAfterControllersNextTurn() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        castHugs(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void additionalLandPermissionDoesNotApplyToOpponent() {
        harness.addToBattlefield(player1, new HugsGrislyGuardian());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player2);

        harness.playLand(player2, 0);

        assertThatThrownBy(() -> harness.playLand(player2, 0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void additionalLandPermissionDoesNotAllowLandsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new HugsGrislyGuardian());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
    }

    private void castHugs(int xValue) {
        harness.setHand(player1, List.of(new HugsGrislyGuardian()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        gs.playCard(gd, player1, 0, xValue, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
