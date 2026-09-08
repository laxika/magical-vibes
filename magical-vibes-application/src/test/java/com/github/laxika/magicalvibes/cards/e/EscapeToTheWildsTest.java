package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EscapeToTheWilds.class, Forest.class, GrizzlyBears.class})
class EscapeToTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top five cards, grants play permission, and adds a land play")
    void exilesTopFiveAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card fourth = new GrizzlyBears();
        Card fifth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new EscapeToTheWilds()));
        addManaForEscape();
        prepareMainPhase();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        Card exiledCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledLand, exiledCreature,
                new Forest(), new GrizzlyBears(), new Forest()));
        Card handLand = new Forest();
        harness.setHand(player1, List.of(new EscapeToTheWilds(), handLand));
        addManaForEscape();
        prepareMainPhase();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.playCardFromExile(gd, player1, exiledLand.getId(), null, null);
        gs.playCard(gd, player1, 0, 0, null, null);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCardFromExile(gd, player1, exiledCreature.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
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
