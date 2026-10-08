package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({WildWasteland.class, Forest.class, Mountain.class})
class WildWastelandTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, exiles the top two cards with play permission")
    void exilesTopTwoCardsWithPlayPermission() {
        Card first = new Forest();
        Card second = new Mountain();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.addToBattlefield(player1, new WildWasteland());

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Skips the controller's draw step")
    void skipsControllersDrawStep() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest()));
        harness.addToBattlefield(player1, new WildWasteland());
        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        harness.passBothPriorities();
        int handSizeAfterUpkeep = gd.playerHands.get(player1.getId()).size();
        int librarySizeAfterUpkeep = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterUpkeep);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeAfterUpkeep);
    }

    @Test
    void skipsDrawStepWithoutOfferingPriorityInIt() {
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest()));
        harness.addToBattlefield(player1, new WildWasteland());
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void opponentsUpkeepDoesNotExileCardsOrSkipTheirDraw() {
        Card top = new Mountain();
        Card remaining = new Forest();
        harness.setLibrary(player2, List.of(top, remaining));
        harness.addToBattlefield(player1, new WildWasteland());
        gd.turnNumber = 2;

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, remaining);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerHands.get(player2.getId())).contains(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exilesOnlyAvailableCardWithoutLosingToEmptyLibrary() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addToBattlefield(player1, new WildWasteland());
        gd.turnNumber = 2;

        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emptyLibraryDoesNotPreventSkippingDrawStep() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new WildWasteland());
        gd.turnNumber = 2;

        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void canPlayExiledLandButCannotExceedNormalLandLimit() {
        Card first = new Forest();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new WildWasteland());
        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void exiledSpellRequiresNormalTimingAndManaCost() {
        Card spell = new WildWasteland();
        harness.setLibrary(player1, List.of(spell, new Forest()));
        harness.addToBattlefield(player1, new WildWasteland());
        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);

        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wild Wasteland")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    void playPermissionExpiresButUnplayedCardsRemainExiled() {
        Card first = new Forest();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new WildWasteland());
        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(first.getId(), second.getId());
        assertThatThrownBy(() -> harness.castFromExile(player2, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvedPermissionSurvivesSourceLeavingBattlefield() {
        Card first = new Forest();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new WildWasteland());
        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void multipleCopiesEachExileTwoCards() {
        Card first = new Forest();
        Card second = new Mountain();
        Card third = new Forest();
        Card fourth = new Mountain();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, remaining));
        harness.addToBattlefield(player1, new WildWasteland());
        harness.addToBattlefield(player1, new WildWasteland());

        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }
}
