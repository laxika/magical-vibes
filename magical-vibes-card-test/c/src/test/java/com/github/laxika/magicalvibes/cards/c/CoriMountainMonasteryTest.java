package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoriMountainMonastery.class, Island.class, Plains.class, Shock.class})
class CoriMountainMonasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Plains or Island")
    void entersTappedWithoutPlainsOrIsland() {
        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());
        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isFalse();
    }

    @Test
    void opponentsPlainsAndIslandDoNotLetItEnterUntapped() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());

        playLand(new CoriMountainMonastery());

        assertThat(findPermanent(player1, "Cori Mountain Monastery").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping it adds red mana")
    void addsRedMana() {
        Permanent monastery = addReadyMonastery();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(monastery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles the top card and lets you play it until the end of your next turn")
    void exilesTopCardWithPlayPermission() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        addReadyMonastery();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());

        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void exilesOnlyTheTopCardAndCanPlayAnExiledLand() {
        Card top = new Plains();
        Card second = new Island();
        harness.setLibrary(player1, List.of(top, second));
        addReadyMonastery();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void exiledLandStillUsesTheNormalLandPlayForTheTurn() {
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top));
        addReadyMonastery();
        playLand(new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    void activationWithEmptyLibraryStillPaysCosts() {
        harness.setLibrary(player1, List.of());
        Permanent monastery = addReadyMonastery();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(monastery.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void playPermissionDoesNotWaiveTheExiledSpellsManaCost() {
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        addReadyMonastery();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        harness.assertLife(player2, 20);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void permissionFromYourTurnSurvivesTheInterveningOpponentsTurn() {
        harness.setHand(player2, List.of());
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top, new Island(), new Plains(), new Island()));
        harness.setLibrary(player2, List.of(new Plains(), new Island(), new Plains()));
        addReadyMonastery();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void permissionFromOpponentsTurnLastsThroughYourNextTurnThenExpires() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card top = new Plains();
        harness.setLibrary(player1, List.of(top, new Island(), new Plains(), new Island()));
        harness.setLibrary(player2, List.of(new Plains(), new Island(), new Plains()));
        addReadyMonastery();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    private void playLand(Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyMonastery() {
        return addCreatureReady(player1, new CoriMountainMonastery());
    }
}
