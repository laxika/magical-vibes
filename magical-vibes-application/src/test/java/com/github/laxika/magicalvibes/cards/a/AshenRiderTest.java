package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AshenRiderTest extends BaseCardTest {

    @Test
    @DisplayName("When Ashen Rider enters, exile target permanent")
    void entersExilesTargetPermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshenRider()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castCreature(player1, 0, 0, forestId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ashen Rider");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("When Ashen Rider dies, exile target permanent")
    void diesExilesTargetPermanent() {
        harness.addToBattlefield(player1, new AshenRider());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        UUID ashenRiderId = harness.getPermanentId(player1, "Ashen Rider");
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castInstant(player2, 0, ashenRiderId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashen Rider");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }
}
