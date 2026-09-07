package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CommuneWithLava.class, Forest.class, GrizzlyBears.class})
class CommuneWithLavaTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top X cards and grants play permission until the end of the next turn")
    void exilesTopXCardsAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        prepareMainPhase();

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    @DisplayName("Allows playing lands and casting creatures from the exiled cards")
    void playsAndCastsFromExile() {
        Card exiledLand = new Forest();
        Card exiledCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledLand, exiledCreature));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        gs.playCardFromExile(gd, player1, exiledLand.getId(), null, null);
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCardFromExile(gd, player1, exiledCreature.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
