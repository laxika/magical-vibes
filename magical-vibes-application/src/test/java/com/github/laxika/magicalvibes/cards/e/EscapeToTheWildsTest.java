package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
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

@CardUsed({EscapeToTheWilds.class, Forest.class, SporecapSpider.class})
class EscapeToTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top five cards, grants play permission, and adds a land play")
    void exilesTopFiveAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new SporecapSpider();
        Card third = new Forest();
        Card fourth = new SporecapSpider();
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new EscapeToTheWilds()));
        addManaForEscape();
        prepareMainPhase();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third, fourth, fifth);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId())
                .containsEntry(fourth.getId(), player1.getId())
                .containsEntry(fifth.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(first.getId(), gd.turnNumber + 2)
                .containsEntry(second.getId(), gd.turnNumber + 2)
                .containsEntry(third.getId(), gd.turnNumber + 2)
                .containsEntry(fourth.getId(), gd.turnNumber + 2)
                .containsEntry(fifth.getId(), gd.turnNumber + 2);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Allows playing an exiled land and casting an exiled creature")
    void playsAndCastsFromExile() {
        Card exiledLand = new Forest();
        Card exiledCreature = new SporecapSpider();
        harness.setLibrary(player1, List.of(exiledLand, exiledCreature,
                new Forest(), new SporecapSpider(), new Forest()));
        Card handLand = new Forest();
        harness.setHand(player1, List.of(new EscapeToTheWilds(), handLand));
        addManaForEscape();
        prepareMainPhase();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castFromExile(player1, exiledLand.getId());
        harness.playLand(player1, 0);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sporecap Spider");
    }

    @Test
    void exilesOnlyAvailableCardsAndStillGrantsAdditionalLandWithEmptyLibrary() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        prepareMainPhase();
        harness.castFromHand(player1, new EscapeToTheWilds(), "{3}{R}{G}");
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        harness.castFromExile(player1, land.getId());

        harness.castFromHand(player1, new EscapeToTheWilds(), "{3}{R}{G}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(3);
    }

    @Test
    void permissionLastsThroughNextTurnButAdditionalLandDoesNot() {
        Card firstLand = new Forest();
        Card remainingLand = new Forest();
        setLibraryForTurnTests(firstLand, remainingLand);
        prepareMainPhase();
        harness.castFromHand(player1, new EscapeToTheWilds(), "{3}{R}{G}");
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player2, firstLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, firstLand.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, remainingLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, remainingLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(remainingLand);
    }

    @Test
    void exiledCreatureStillRequiresManaAndNormalTiming() {
        Card creature = new SporecapSpider();
        harness.setLibrary(player1, List.of(creature));
        prepareMainPhase();
        harness.castFromHand(player1, new EscapeToTheWilds(), "{3}{R}{G}");
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sporecap Spider");
    }

    @Test
    @CardUsed(TimeWarp.class)
    void permissionExpiresAfterCastersImmediateExtraTurn() {
        Card land = new Forest();
        setLibraryForTurnTests(land, new Forest());
        prepareMainPhase();
        harness.castFromHand(player1, new EscapeToTheWilds(), "{3}{R}{G}");
        harness.passBothPriorities();
        castTimeWarp(player1.getId());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    @Test
    @CardUsed(TimeWarp.class)
    void permissionSurvivesOpponentsExtraTurnUntilCastersNextTurn() {
        Card land = new Forest();
        setLibraryForTurnTests(land, new Forest());
        prepareMainPhase();
        harness.castFromHand(player1, new EscapeToTheWilds(), "{3}{R}{G}");
        harness.passBothPriorities();
        castTimeWarp(player2.getId());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    private void castTimeWarp(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void setLibraryForTurnTests(Card first, Card second) {
        harness.setLibrary(player1, List.of(first, second, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
    }

    private void addManaForEscape() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
